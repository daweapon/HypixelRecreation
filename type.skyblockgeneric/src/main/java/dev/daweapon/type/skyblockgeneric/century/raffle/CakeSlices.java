package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.century.CenturyCakes;
import dev.daweapon.type.skyblockgeneric.century.raffle.CenturyRaffleConfig.FeedingReward;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class CakeSlices {
    private static final Set<UUID> FEEDING = ConcurrentHashMap.newKeySet();

    private CakeSlices() {
    }

    public static boolean feed(SkyBlockPlayer feeder, SkyBlockPlayer target) {
        ItemType held = new SkyBlockItem(feeder.getItemInMainHand()).getAttributeHandler().getPotentialType();
        CenturyTeam slice = CenturyTeam.ofSlice(held);
        if (slice == null) return false;
        CenturyEvent.Window window = CenturyEvent.active();
        if (window == null) {
            feeder.sendMessage("<c>This slice has spoiled... We should have celebrated earlier.");
            return true;
        }
        if (CenturyRaffle.team(feeder) == null) {
            feeder.sendMessage("<c>Talk to <a>Rezzus <c>in the Hub to join a Raffle team first!");
            return true;
        }
        CenturyTeam targetTeam = CenturyRaffle.team(target);
        if (targetTeam == null) {
            feeder.sendMessage("<c>That player hasn't joined a Raffle team yet!");
            return true;
        }
        if (targetTeam != slice) {
            feeder.sendMessage("<c>You can only feed this slice to a player on the " + slice.display() + " <c>team!");
            return true;
        }
        if (!FEEDING.add(feeder.getUuid())) return true;
        ItemStack hand = feeder.getItemInMainHand();
        feeder.setItemInMainHand(hand.amount() > 1 ? hand.withAmount(hand.amount() - 1) : ItemStack.AIR);
        int period = RaffleClock.taskPeriod(window, System.currentTimeMillis());
        int limit = CenturyRaffleConfig.get().fedSlicesPerPeriod();
        CenturyRaffle.async(() -> {
            boolean fed = RaffleStore.feed(window.id(), target.getUuid(), period, limit);
            CenturyRaffle.sync(() -> {
                FEEDING.remove(feeder.getUuid());
                if (!fed) {
                    feeder.addAndUpdateItem(slice.slice());
                    feeder.sendMessage("<c>" + target.getUsername() + " is too full to eat any more cake right now!");
                    return;
                }
                FeedingReward reward = LootRoll.pick(feeder.getUuid(), CenturyRaffleConfig.get().feeding(),
                        FeedingReward::itemType, FeedingReward::weight, ThreadLocalRandom.current());
                String rewardName = describe(reward);
                feeder.sendMessage("<d><l>YUM! <r><e>You fed <7>" + target.getUsername() + " <e>a slice of " + slice.flavour()
                        + " <e>cake! You both got " + rewardName + "<e>!");
                target.sendMessage("<d><l>YUM! <r><7>" + feeder.getUsername() + " <e>fed you a slice of " + slice.flavour()
                        + " <e>cake! You both got " + rewardName + "<e>!");
                apply(feeder, reward);
                apply(target, reward);
                CenturyRaffle.grantTickets(feeder, 1, "<d><l>CAKE! <r><e>You gained <b>+1 Raffle Ticket <e>for sharing cake!");
                CenturyRaffle.grantTickets(target, 1, "<d><l>CAKE! <r><e>You gained <b>+1 Raffle Ticket <e>for eating cake!");
                CenturyRaffle.progress(feeder, RaffleAction.of(RaffleAction.Trigger.FEED_SLICE));
                CenturyRaffle.progress(target, RaffleAction.of(RaffleAction.Trigger.EAT_CAKE));
            });
        });
        return true;
    }

    static void apply(SkyBlockPlayer player, FeedingReward reward) {
        switch (reward.kind()) {
            case CENTURY_CAKE -> player.addAndUpdateItem(CenturyCakes.random(player.getUuid()));
            case CAKE_SOUL -> player.addAndUpdateItem(CenturyCakes.randomCakeSoul(player.getUuid()));
            case TICKETS -> CenturyRaffle.grantTickets(player, reward.amount(),
                    "<d><l>CAKE! <r><e>You gained <b>+" + reward.amount() + " Raffle Ticket" + (reward.amount() == 1 ? "" : "s") + "<e>!");
            case SKILL_XP -> {
                if (reward.skill() == null) return;
                var skills = player.getSkills();
                skills.setRaw(player, reward.skill(), skills.getRaw(reward.skill()) + reward.amount());
            }
            case COINS -> player.addCoins(reward.amount());
            case ITEM -> {
                ItemType item = reward.itemType();
                if (item != null) player.addAndUpdateItem(item);
            }
        }
    }

    static String describe(FeedingReward reward) {
        return switch (reward.kind()) {
            case CENTURY_CAKE -> "<a>a Random Century Cake";
            case CAKE_SOUL -> "<d>a Random Cake Soul";
            case TICKETS -> "<b>" + reward.amount() + " Raffle Ticket" + (reward.amount() == 1 ? "" : "s");
            case SKILL_XP -> "<3>+" + StringUtility.commaify(reward.amount()) + " " + StringUtility.toNormalCase(String.valueOf(reward.skill())) + " XP";
            case COINS -> "<6>" + StringUtility.commaify(reward.amount()) + " Coins";
            case ITEM -> {
                ItemType item = reward.itemType();
                yield "<a>" + (item == null ? "nothing" : item.getDisplayName());
            }
        };
    }
}
