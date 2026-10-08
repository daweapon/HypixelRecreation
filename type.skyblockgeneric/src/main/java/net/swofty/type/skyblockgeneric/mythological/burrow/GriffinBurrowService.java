package net.swofty.type.skyblockgeneric.mythological.burrow;

import net.swofty.type.skyblockgeneric.carnival.CarnivalPerk;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mythological.GriffinPet;
import net.swofty.type.skyblockgeneric.mythological.MythologicalBestiary;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreature;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.mythological.MythologicalLootShare;
import net.swofty.type.skyblockgeneric.mythological.MythologicalVariant;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalCoins;
import net.swofty.type.skyblockgeneric.mythological.gear.MythologicalGear;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Diana's Mythological Ritual in the Hub: each player holding a Griffin spade with a Griffin
 * equipped follows their own chain of Griffin Burrows, digging up treasure or Mythological
 * creatures along the way.
 */
public final class GriffinBurrowService implements MythologicalCreatureMob.Listener {
    public static final GriffinBurrowService INSTANCE = new GriffinBurrowService();

    private static final double START_MIN_DISTANCE = 10;
    private static final double START_MAX_DISTANCE = 50;
    private static final double NEXT_MIN_DISTANCE = 20;
    private static final double NEXT_MAX_DISTANCE = 70;
    private static final List<String> DIG_EXCLAMATIONS = List.of("Danger!", "Oi!", "Uh oh!", "Woah!", "Yikes!", "Good Grief!");

    private final Map<UUID, BurrowChain> chains = new ConcurrentHashMap<>();
    private final Map<UUID, MythologicalCreature> forcedCreatures = new ConcurrentHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean();

    private GriffinBurrowService() {
    }

    public void forceNextCreature(UUID player, MythologicalCreature creature) {
        forcedCreatures.put(player, creature);
    }

    public static boolean isRitualActive() {
        return HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_HUB
                && ElectionPerks.isActive(SkyBlockMayor.Perk.MYTHOLOGICAL_RITUAL);
    }

    public static boolean isHoldingSpade(SkyBlockPlayer player) {
        return SpadeTier.held(player).isPresent();
    }

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager().buildTask(this::tick)
                .delay(TaskSchedule.seconds(5))
                .repeat(TaskSchedule.tick(10))
                .schedule();
    }

    private void tick() {
        if (!isRitualActive()) {
            chains.keySet().forEach(this::forget);
            return;
        }
        chains.keySet().stream()
                .filter(playerId -> SkyBlockGenericLoader.getFromUUID(playerId) == null)
                .toList()
                .forEach(this::forget);
        GriffinBurrowSpots.ensureSampled(HypixelConst.getInstanceContainer());
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            BurrowChain chain = chains.get(player.getUuid());
            if (chain == null || !isHoldingSpade(player)) continue;
            BurrowEffects.marker(player, chain.getCurrent());
            BurrowEffects.footprint(player, chain.getCurrent());
            if (chain.getPrevious() != null) {
                BurrowEffects.arrow(player, chain.getPrevious(), chain.getCurrent().getPosition());
            }
        }
    }

    public boolean echo(SkyBlockPlayer player) {
        if (!canUse(player)) return false;
        Optional<BurrowChain> chain = chainOf(player);
        if (chain.isEmpty()) {
            player.sendMessage("<e>Your Griffin is still sniffing out burrows, try again in a moment!");
            return false;
        }
        BurrowEffects.echo(player, chain.get().getCurrent().getPosition());
        return true;
    }

    public boolean dig(SkyBlockPlayer player, Point block) {
        BurrowChain chain = chains.get(player.getUuid());
        if (chain == null || !chain.getCurrent().isAt(block) || !isHoldingSpade(player)) return false;
        if (!canUse(player)) return true;

        GriffinBurrow burrow = chain.getCurrent();
        Rarity griffin = GriffinPet.equippedRarity(player).orElseThrow();
        switch (burrow.getKind()) {
            case START -> excavate(player, chain);
            case TREASURE -> {
                giveTreasure(player);
                excavate(player, chain);
            }
            case MOB -> {
                if (!burrow.isOpened()) {
                    summonDefenders(player, chain, burrow, griffin);
                } else if (burrow.isDefended()) {
                    player.sendMessage("<c>Defeat the burrow defenders in order to dig it!");
                } else {
                    excavate(player, chain);
                }
            }
        }
        BurrowEffects.dig(player, burrow.getPosition());
        return true;
    }

    public void reset(SkyBlockPlayer player) {
        BurrowChain chain = chains.remove(player.getUuid());
        if (chain != null) dismiss(chain.getCurrent());
        player.sendMessage("<a>Your Griffin Burrows have been reset!");
    }

    public void forget(UUID playerId) {
        BurrowChain chain = chains.remove(playerId);
        if (chain != null) dismiss(chain.getCurrent());
    }

    private boolean canUse(SkyBlockPlayer player) {
        if (!isRitualActive()) {
            player.sendMessage("<c>Griffin Burrows only appear in the Hub while Diana's <2>Mythological Ritual <c>is active!");
            return false;
        }
        if (GriffinPet.equippedRarity(player).isEmpty()) {
            player.sendMessage("<c>You need to have a Griffin pet equipped to find Griffin Burrows!");
            return false;
        }
        return true;
    }

    private Optional<BurrowChain> chainOf(SkyBlockPlayer player) {
        BurrowChain existing = chains.get(player.getUuid());
        if (existing != null) return Optional.of(existing);
        if (!GriffinBurrowSpots.isReady()) {
            GriffinBurrowSpots.ensureSampled(HypixelConst.getInstanceContainer());
            return Optional.empty();
        }
        return newChain(player);
    }

    private Optional<BurrowChain> newChain(SkyBlockPlayer player) {
        return GriffinBurrowSpots.near(player.getPosition(), START_MIN_DISTANCE, START_MAX_DISTANCE, ThreadLocalRandom.current())
                .map(start -> {
                    int length = SpadeTier.chainLength(new SkyBlockItem(player.getItemInMainHand()));
                    BurrowChain chain = new BurrowChain(length, start);
                    chains.put(player.getUuid(), chain);
                    return chain;
                });
    }

    private void excavate(SkyBlockPlayer player, BurrowChain chain) {
        MythologicalCoins.onBurrowDug(player);
        int number = chain.getExcavated() + 1;
        if (chain.isLastBurrow()) {
            chain.finish();
            player.sendMessage("<e>You finished the Griffin burrow chain! <7>(" + number + "/" + chain.getLength() + ")");
            chains.remove(player.getUuid());
            newChain(player);
            return;
        }

        player.sendMessage("<e>You dug out a Griffin Burrow! <7>(" + number + "/" + chain.getLength() + ")");
        BlockVec from = chain.getCurrent().getPosition();
        Optional<BlockVec> next = GriffinBurrowSpots.near(from, NEXT_MIN_DISTANCE, NEXT_MAX_DISTANCE, ThreadLocalRandom.current());
        if (next.isEmpty()) {
            chains.remove(player.getUuid());
            return;
        }
        chain.advance(next.get(), BurrowKind.rollFollowUp(ThreadLocalRandom.current()));
    }

    public void giveTreasure(SkyBlockPlayer player) {
        BurrowTreasure treasure = SpadeTier.held(player).orElse(SpadeTier.ANCESTRAL).roll(ThreadLocalRandom.current());
        if (treasure.item() == null) {
            long coins = MythologicalCoins.fromBurrow(player, treasure.coins());
            if (coins <= 0) {
                player.sendMessage("<6><l>Wow!</l> <e>You dug out <6>" + String.format("%,d", treasure.coins())
                        + " coins<e>, but your <6>Crown of Avarice <e>ate them!");
                return;
            }
            player.addCoins(coins);
            player.sendMessage("<6><l>Wow!</l> <e>You dug out <6>" + String.format("%,d", coins) + " coins<e>!");
            return;
        }
        SkyBlockItem item = new SkyBlockItem(treasure.item());
        player.addAndUpdateItem(item);
        String color = item.getAttributeHandler().getRarity().getColor().asHexString();
        player.sendMessage("<6><l>RARE DROP!</l> <e>You dug out a <color:" + color + ">" + item.getDisplayName() + "<e>!");
    }

    private void summonDefenders(SkyBlockPlayer player, BurrowChain chain, GriffinBurrow burrow, Rarity griffin) {
        double tracking = player.getStatistics().allStatistics().getOverall(ItemStatistic.TRACKING)
                + Carnivals.perkValue(player, CarnivalPerk.ELUSIVE_HUNTER)
                + MythologicalGear.burrowTracking(player);
        if (GriffinPet.isMythic(player)) tracking += chain.getExcavated();

        MythologicalCreature forced = forcedCreatures.remove(player.getUuid());
        MythologicalCreature creature = forced != null ? forced
                : MythologicalCreature.roll(griffin, tracking, ThreadLocalRandom.current());
        MythologicalVariant variant = creature.variant(griffin)
                .or(() -> creature.allVariants().stream().findFirst())
                .orElseThrow();
        Instance instance = player.getInstance();
        for (int i = 0; i < creature.getGroupSize(); i++) {
            MythologicalCreatureMob mob = MythologicalCreatureMob.of(creature, variant);
            mob.bind(player.getUuid(), this);
            mob.setInstance(instance, burrow.getPosition().add(0.5 + i * 0.6, 1, 0.5).asPos());
            burrow.getDefenders().add(mob);
            if (creature == MythologicalCreature.MINOS_INQUISITOR) MythologicalLootShare.track(mob, player);
        }
        if (creature == MythologicalCreature.SIAMESE_LYNXES && burrow.getDefenders().size() == 2) {
            MythologicalCreatureMob.pairAsTwins(burrow.getDefenders().get(0), burrow.getDefenders().get(1));
        }
        burrow.setOpened(true);

        String exclamation = DIG_EXCLAMATIONS.get(ThreadLocalRandom.current().nextInt(DIG_EXCLAMATIONS.size()));
        player.sendMessage("<c><l>" + exclamation + "</l> <e>You dug out a <2>" + creature.getDisplayName() + "<e>!");
    }

    private static void dismiss(GriffinBurrow burrow) {
        burrow.getDefenders().forEach(defender -> {
            MythologicalLootShare.forget(defender);
            defender.remove();
        });
        burrow.getDefenders().clear();
    }

    @Override
    public void onSlain(MythologicalCreatureMob mob) {
        if (mob.getLastDamageSource() != null && mob.getLastDamageSource().getAttacker() instanceof SkyBlockPlayer killer) {
            MythologicalBestiary.updateKillsXP(killer);
        }
        if (mob.getDigger() == null) return;
        SkyBlockPlayer digger = SkyBlockGenericLoader.getFromUUID(mob.getDigger());
        if (digger != null) MythologicalLootShare.share(mob, digger);
    }

    @Override
    public void onEscaped(MythologicalCreatureMob mob) {
        MythologicalLootShare.forget(mob);
        if (mob.getDigger() == null || mob.getCreature() != MythologicalCreature.MINOS_INQUISITOR) return;
        BurrowChain chain = chains.remove(mob.getDigger());
        if (chain != null) dismiss(chain.getCurrent());
        SkyBlockPlayer digger = SkyBlockGenericLoader.getFromUUID(mob.getDigger());
        if (digger != null) {
            digger.sendMessage("<c>The " + mob.getDisplayName() + " <c>escaped and your Griffin Burrow chain was lost!");
        }
    }
}
