package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.century.CenturyCakes;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RaffleClaims {
    private RaffleClaims() {
    }

    public static void claimResult(SkyBlockPlayer player, RaffleSnapshot snapshot, RaffleSnapshot.Row row, Runnable after) {
        if (!row.claimable()) return;
        String eventId = snapshot.window().id();
        CenturyRaffle.async(() -> {
            if (!RaffleStore.claimResult(eventId, player.getUuid(), row.key())) {
                CenturyRaffle.sync(() -> player.sendMessage("<c>You already claimed this raffle!"));
                return;
            }
            CenturyRaffle.sync(() -> {
                player.sendMessage("<7>Grabbing your prize...");
                RaffleType type = RaffleClock.typeOf(row.key());
                ItemType forced = LootRoll.claim(player.getUuid(), allPrizeItems());
                if (forced != null || row.tier() != null) {
                    SkyBlockItem prize = forced != null ? new SkyBlockItem(forced) : prize(player.getUuid(), row.tier());
                    player.addAndUpdateItem(prize);
                    player.sendMessage("<e>You claimed your prize from <8>" + row.label() + "<e>!");
                    String broadcast = "<6><l>RAFFLE! <r><7>" + player.getUsername() + " <e>won <a>" + prize.getDisplayName()
                            + " <e>in <8>" + row.label() + "<e>!";
                    SkyBlockGenericLoader.getLoadedPlayers().forEach(online -> online.sendMessage(broadcast));
                } else {
                    int slices = type.consolationSlices(row.entered());
                    for (int i = 0; i < slices; i++) player.addAndUpdateItem(CenturyRaffle.randomSlice(player.getUuid()));
                    player.sendMessage(slices > 0
                            ? "<e>You claimed your compensation prize from <8>" + row.label() + "<e>!"
                            : "<7>You didn't win anything in <8>" + row.label() + "<7>. Better luck next time!");
                }
                if (type == RaffleType.BIG_ONE && row.entered() >= RaffleType.BIG_ONE_CAKE_TICKETS) {
                    player.addAndUpdateItem(CenturyCakes.random(player.getUuid()));
                    player.sendMessage("<e>You claimed your compensation prize from <8>" + row.label() + "<e>!");
                }
                after.run();
            });
        });
    }

    public static void claimMilestone(SkyBlockPlayer player, RaffleSnapshot snapshot, RaffleMilestone milestone, Runnable after) {
        if (snapshot.profile().tickets() < milestone.tickets() || snapshot.profile().claimedMilestone(milestone.slices())) return;
        String eventId = snapshot.window().id();
        CenturyRaffle.async(() -> {
            if (!RaffleStore.claimOnce(eventId, player.getUuid(), milestone.field())) return;
            snapshot.profile().claimMilestone(milestone.slices());
            CenturyRaffle.sync(() -> {
                if (milestone.coins() > 0) player.addCoins(milestone.coins());
                if (milestone.emblemKey() != null) CenturyRaffle.unlockEmblem(player, milestone.emblemKey());
                ItemType item = milestone.item();
                if (item != null) player.addAndUpdateItem(item);
                for (int i = 0; i < milestone.slices(); i++) player.addAndUpdateItem(CenturyRaffle.randomSlice(player.getUuid()));
                player.sendMessage("<e>You claimed the <6>Raffle Milestone " + milestone.numeral() + " <e>reward, gg!");
                after.run();
            });
        });
    }

    public static void claimMemento(SkyBlockPlayer player, RaffleSnapshot snapshot, Runnable after) {
        CenturyTeam winner = snapshot.leadingTeam();
        if (snapshot.active() || winner == null || snapshot.mementoClaimed() || snapshot.profile().team() == null) return;
        String eventId = snapshot.window().id();
        CenturyRaffle.async(() -> {
            if (!RaffleStore.claimOnce(eventId, player.getUuid(), RaffleProfile.MEMENTO)) return;
            snapshot.profile().flag(RaffleProfile.MEMENTO);
            CenturyRaffle.sync(() -> {
                player.addAndUpdateItem(winner.memento());
                player.sendMessage("<e>You claimed a <6>Cake Slice of the Century<e>! The " + winner.display() + " <e>team won!");
                after.run();
            });
        });
    }

    public static void claimSimonCake(SkyBlockPlayer player, Runnable onFirst, Runnable onRepeat) {
        CenturyEvent.Window window = CenturyEvent.active();
        if (window == null || !RaffleStore.available()) return;
        CenturyRaffle.async(() -> {
            boolean first = RaffleStore.claimOnce(window.id(), player.getUuid(), RaffleProfile.SIMON);
            CenturyRaffle.sync(() -> {
                if (!first) {
                    onRepeat.run();
                    return;
                }
                player.addAndUpdateItem(CenturyCakes.random(player.getUuid()));
                onFirst.run();
            });
        });
    }

    static SkyBlockItem prize(UUID player, PrizeTier tier) {
        List<String> pool = CenturyRaffleConfig.get().pool(tier);
        String picked = LootRoll.pick(player, pool, RaffleClaims::itemOf, _ -> 1D, ThreadLocalRandom.current());
        return resolve(player, picked);
    }

    public static SkyBlockItem resolve(UUID player, String prize) {
        return switch (prize) {
            case CenturyRaffleConfig.RANDOM_CENTURY_CAKE -> new SkyBlockItem(CenturyCakes.random(player));
            case CenturyRaffleConfig.RANDOM_CAKE_SOUL -> CenturyCakes.randomCakeSoul(player);
            default -> new SkyBlockItem(Objects.requireNonNull(ItemType.get(prize)));
        };
    }

    static @Nullable ItemType itemOf(String prize) {
        return CenturyRaffleConfig.isSpecial(prize) ? null : ItemType.get(prize);
    }

    public static boolean isPrize(ItemType item) {
        return allPrizeItems().contains(item);
    }

    static List<ItemType> allPrizeItems() {
        List<ItemType> items = new ArrayList<>();
        for (PrizeTier tier : PrizeTier.values()) {
            for (String prize : CenturyRaffleConfig.get().pool(tier)) {
                ItemType item = itemOf(prize);
                if (item != null) items.add(item);
            }
        }
        items.addAll(CenturyCakes.all());
        items.add(ItemType.CAKE_SOUL);
        return items;
    }
}
