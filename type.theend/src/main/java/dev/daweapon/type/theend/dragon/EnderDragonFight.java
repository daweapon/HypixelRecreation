package dev.daweapon.type.theend.dragon;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class EnderDragonFight {
    public static final int CRYSTALS = 6;
    public static final int CRYSTAL_RESPAWNS = 3;
    public static final int EGG_RESPAWN_TICKS = 1200;
    public static final double KNOCKBACK_RADIUS = 30;
    public static final double CRYSTAL_DAMAGE_CREDIT = 1000;

    private enum Phase { IDLE, SUMMONING, ACTIVE, EGG_RESPAWN }

    private static final EyeLedger LEDGER = new EyeLedger();
    private static volatile boolean active;
    private static Phase phase = Phase.IDLE;
    private static EnderDragonMob dragon;
    private static final List<DragonCrystalMob> crystals = new ArrayList<>();
    private static final List<Pos> emptySites = new ArrayList<>();
    private static int respawnsLeft;
    private static Map<UUID, Integer> eyesAtStart = Map.of();
    private static BossBar bar;
    private static Task barTask;
    private static final Set<UUID> barViewers = new HashSet<>();
    private static final int BAR_TICKS = 10;
    private static int gateStage;

    private EnderDragonFight() {
    }

    public static boolean isActive() {
        return active;
    }

    public static EyeLedger ledger() {
        return LEDGER;
    }

    public static boolean acceptsEyes() {
        return phase == Phase.IDLE;
    }

    public static boolean eggRespawning() {
        return phase == Phase.EGG_RESPAWN;
    }

    public static EnderDragonMob dragon() {
        return dragon;
    }

    public static int crystalsNear(Pos at, double range) {
        crystals.removeIf(crystal -> crystal.isRemoved());
        return (int) crystals.stream().filter(crystal -> crystal.getPosition().distance(at) <= range).count();
    }

    public static int liveCrystals() {
        crystals.removeIf(crystal -> crystal.isRemoved());
        return crystals.size();
    }

    public static void onAltarFilled(Instance instance) {
        if (phase != Phase.IDLE || !LEDGER.complete()) return;
        phase = Phase.SUMMONING;
        DragonSpawnAnimation.play(instance, () -> spawn(instance));
    }

    private static void spawn(Instance instance) {
        DragonVariant variant = DragonVariant.roll(LEDGER.recombobulated(), ThreadLocalRandom.current());
        eyesAtStart = Map.copyOf(LEDGER.eyesByPlayer());
        active = true;
        phase = Phase.ACTIVE;
        EnderDragonMob mob = new EnderDragonMob(variant, EnderDragonFight::crystalsNear, EnderDragonFight::defeated);
        dragon = mob;
        mob.setInstance(instance, DragonArena.EGG);
        resetFrames(instance);
        convertSleepingEyes(instance);
        knockPlayersBack(instance);
        spawnCrystals(instance);
        gateStage = 0;
        startBar(instance, mob);
        broadcast(instance, Text.of("<5>The {} <5>has spawned!", variant.displayName()));
    }

    private static void convertSleepingEyes(Instance instance) {
        for (var player : instance.getPlayers()) {
            if (!(player instanceof SkyBlockPlayer skyBlockPlayer)) continue;
            int converted = 0;
            while (skyBlockPlayer.removeItemFromPlayer(ItemType.SLEEPING_EYE, 1)) converted++;
            if (converted == 0) continue;
            skyBlockPlayer.addAndUpdateItem(ItemType.REMNANT_OF_THE_EYE, converted);
            skyBlockPlayer.sendMessage(Text.of("<5>Your Sleeping Eyes have been awoken by the magic of the Dragon. They are now Remnants of the Eye!"));
        }
    }

    private static void knockPlayersBack(Instance instance) {
        for (var player : instance.getPlayers()) {
            Vec away = player.getPosition().sub(DragonArena.EGG).asVec();
            if (away.length() > KNOCKBACK_RADIUS) continue;
            Vec direction = away.length() < 0.1 ? new Vec(1, 0, 0) : away.withY(0).normalize();
            player.setVelocity(direction.mul(30).withY(12));
        }
    }

    private static void spawnCrystals(Instance instance) {
        crystals.clear();
        emptySites.clear();
        respawnsLeft = CRYSTAL_RESPAWNS;
        List<Pos> sites = new ArrayList<>(DragonArena.CRYSTAL_SITES);
        java.util.Collections.shuffle(sites);
        for (int i = 0; i < sites.size(); i++) {
            if (i < CRYSTALS) placeCrystal(instance, sites.get(i));
            else emptySites.add(sites.get(i));
        }
    }

    private static void placeCrystal(Instance instance, Pos site) {
        DragonCrystalMob[] holder = new DragonCrystalMob[1];
        holder[0] = new DragonCrystalMob(player -> crystalDestroyed(instance, site, player));
        holder[0].setInstance(instance, site);
        crystals.add(holder[0]);
    }

    private static void crystalDestroyed(Instance instance, Pos site, SkyBlockPlayer player) {
        if (dragon != null) dragon.credit(player.getUuid(), CRYSTAL_DAMAGE_CREDIT);
        ItemType fragment = ItemType.get("CRYSTAL_FRAGMENT");
        if (fragment != null) player.addAndUpdateItem(fragment, 1);
        broadcast(instance, Text.of("<5>{} <d>has destroyed an Ender Crystal!", player.getUsername()));
        if (respawnsLeft <= 0 || phase != Phase.ACTIVE) return;
        respawnsLeft--;
        int delay = 1200 + ThreadLocalRandom.current().nextInt(1200);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (phase != Phase.ACTIVE) return;
            placeCrystal(instance, site);
            broadcast(instance, Text.of("<d>An Ender Crystal has respawned!"));
        }).delay(TaskSchedule.tick(delay)).schedule();
    }

    private static void defeated(EnderDragonMob finished) {
        Instance instance = finished.getInstance();
        active = false;
        phase = Phase.EGG_RESPAWN;
        stopBar();
        DragonVariant variant = finished.variant();
        List<Map.Entry<UUID, Double>> ranking = finished.getDamageDealt().entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue(Comparator.reverseOrder())).toList();
        if (instance != null) announceDown(instance, finished, variant, ranking);
        if (instance != null) DragonLootSite.build(instance, DragonLootSite.nearestFloor(finished.getPosition()));
        double top = ranking.isEmpty() ? 0 : ranking.getFirst().getValue();
        for (int i = 0; i < ranking.size(); i++) {
            SkyBlockPlayer player = SkyBlockGenericLoader.getFromUUID(ranking.get(i).getKey());
            if (player == null) continue;
            reward(player, finished, variant, i + 1, ranking.get(i).getValue(), top);
        }
        crystals.forEach(crystal -> {
            if (!crystal.isRemoved()) crystal.remove();
        });
        crystals.clear();
        LEDGER.clear();
        eyesAtStart = Map.of();
        dragon = null;
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            phase = Phase.IDLE;
            if (instance != null) {
                DragonEgg.restore(instance);
                DragonGlass.restore(instance);
                DragonLootSite.restore(instance);
                broadcast(instance, Text.of("<5>The Dragon Egg has respawned!"));
            }
        }).delay(TaskSchedule.tick(EGG_RESPAWN_TICKS)).schedule();
    }

    private static void reward(SkyBlockPlayer player, EnderDragonMob mob, DragonVariant variant, int rank,
                               double damage, double top) {
        int eyes = eyesAtStart.getOrDefault(player.getUuid(), 0);
        int quality = LootQuality.quality(rank, damage, top, eyes);
        if (mob.isProvokedBy(player) && !player.equals(killerOf(mob))) {
            HypixelEventHandler.callCustomEvent(new PlayerKilledSkyBlockMobEvent(player, mob));
            player.getSkills().increase(player, SkillCategories.COMBAT, (double) variant.combatXp());
        }
        List<DragonLoot.Reward> rewards = DragonLoot.plan(variant, quality, eyes, rank, damage,
                (item, chance) -> LootRoll.roll(player.getUuid(), item, luckAdjusted(player, mob, item, chance)));
        for (DragonLoot.Reward reward : rewards) {
            SkyBlockItem item = new SkyBlockItem(reward.item(), reward.amount());
            if (reward.rarity() != null) item.getAttributeHandler().setRarity(reward.rarity());
            if (reward.major()) CrimsonMobLoot.announceRareDrop(player, mob, item);
            DragonLootSite.drop(mob.getInstance(), player, item);
        }
        Essences.add(player, EssenceType.DRAGON, variant.essenceReward());
        DragonProgress.recordKill(player, variant);
    }

    private static double luckAdjusted(SkyBlockPlayer player, EnderDragonMob mob, ItemType item, double chance) {
        var statistics = player.getStatistics().allStatistics(player, mob);
        double luck = statistics.getOverall(ItemStatistic.MAGIC_FIND);
        if (item == ItemType.ENDER_DRAGON_PET) luck += statistics.getOverall(ItemStatistic.PET_LUCK);
        return chance * (1 + luck / 100);
    }

    private static void announceDown(Instance instance, EnderDragonMob mob, DragonVariant variant,
                                     List<Map.Entry<UUID, Double>> ranking) {
        SkyBlockPlayer killer = killerOf(mob);
        DragonDownMessage.Name finalBlow = killer == null ? null : nameOf(killer);
        List<DragonDownMessage.Damager> top = ranking.stream().limit(3)
                .map(entry -> new DragonDownMessage.Damager(nameOf(entry.getKey()), entry.getValue())).toList();
        for (Player viewer : instance.getPlayers()) {
            int position = 0;
            double own = 0;
            for (int i = 0; i < ranking.size(); i++) {
                if (!ranking.get(i).getKey().equals(viewer.getUuid())) continue;
                position = i + 1;
                own = ranking.get(i).getValue();
            }
            DragonDownMessage.lines(variant.displayName(), finalBlow, top, position, own, variant.essenceReward())
                    .forEach(line -> viewer.sendMessage(Text.parse(line)));
        }
    }

    private static DragonDownMessage.Name nameOf(UUID uuid) {
        Player online = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (online instanceof SkyBlockPlayer player) return nameOf(player);
        return new DragonDownMessage.Name("<7>Disconnected", "Disconnected");
    }

    private static DragonDownMessage.Name nameOf(SkyBlockPlayer player) {
        boolean ranked = player.getRank() != dev.daweapon.type.generic.user.categories.Rank.DEFAULT;
        return new DragonDownMessage.Name(Text.of("{}{}", player.getRankPrefix(), player.getUsername()).serialize(),
                (ranked ? "[MVP+] " : "") + player.getUsername());
    }

    private static SkyBlockPlayer killerOf(EnderDragonMob mob) {
        return mob.getLastDamageSource() != null && mob.getLastDamageSource().getAttacker() instanceof SkyBlockPlayer p
                ? p : null;
    }

    private static void resetFrames(Instance instance) {
        for (Pos frame : DragonArena.ALTAR_FRAMES) {
            Block block = instance.getBlock(frame);
            if (block.name().equals("minecraft:end_portal_frame")) instance.setBlock(frame, block.withProperty("eye", "false"));
        }
        LEDGER.clear();
    }

    public static void abort() {
        stopBar();
        Instance current = HypixelConst.getInstanceContainer();
        if (current != null) {
            DragonLootSite.restore(current);
            DragonGlass.restore(current);
        }
        if (dragon != null) dragon.abandon();
        crystals.forEach(crystal -> {
            if (!crystal.isRemoved()) crystal.remove();
        });
        crystals.clear();
        dragon = null;
        active = false;
        phase = Phase.IDLE;
        LEDGER.clear();
    }

    private static void startBar(Instance instance, EnderDragonMob mob) {
        stopBar();
        bar = BossBar.bossBar(Component.empty(), 1f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);
        barTask = MinecraftServer.getSchedulerManager().buildTask(() -> tickBar(instance, mob))
                .repeat(TaskSchedule.tick(BAR_TICKS)).schedule();
        tickBar(instance, mob);
    }

    private static void tickBar(Instance instance, EnderDragonMob mob) {
        if (bar == null) return;
        if (mob.isRemoved() || mob.isDead()) {
            stopBar();
            return;
        }
        announceGate(instance, mob);
        float max = mob.getMaxHealth();
        bar.progress(Math.max(0f, Math.min(1f, mob.getHealth() / max)));
        bar.name(Text.of("<c>{}", mob.variant().displayName()).asComponent());
        Set<UUID> present = new HashSet<>();
        for (Player player : instance.getPlayers()) {
            present.add(player.getUuid());
            if (barViewers.add(player.getUuid())) player.showBossBar(bar);
        }
        for (UUID uuid : new ArrayList<>(barViewers)) {
            if (present.contains(uuid)) continue;
            barViewers.remove(uuid);
            Player gone = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
            if (gone != null) gone.hideBossBar(bar);
        }
    }

    private static void announceGate(Instance instance, EnderDragonMob mob) {
        double total = mob.getDamageDealt().values().stream().mapToDouble(Double::doubleValue).sum();
        String[] messages = DragonGate.messagesFor(gateStage, total);
        for (String message : messages) broadcast(instance, Text.of("<d>{}", message));
        gateStage += messages.length;
    }

    private static void stopBar() {
        if (barTask != null) barTask.cancel();
        barTask = null;
        if (bar == null) return;
        for (UUID uuid : barViewers) {
            Player viewer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
            if (viewer != null) viewer.hideBossBar(bar);
        }
        barViewers.clear();
        bar = null;
    }

    private static void broadcast(Instance instance, Text message) {
        instance.getPlayers().forEach(player -> player.sendMessage(message));
    }

    public static Instance instance() {
        return HypixelConst.getInstanceContainer();
    }
}
