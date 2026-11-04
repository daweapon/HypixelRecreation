package dev.daweapon.type.skyblockgeneric.spidersden;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobLoot;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden.MobArachne;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.always;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.chance;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.pet;

/**
 * The Arachne boss fight in Arachne's Sanctuary. Placing 4 Arachne's Calling (or 1 Arachne Crystal) on the altar
 * wakes her. The numbers come from the wiki (Arachne, Arachne's Calling, Arachne Fragment) and player videos.
 * The altar block and her spawn point were measured in the captured world; the 1 second dialogue pacing and the
 * dialogue timing are estimates.
 */
public final class ArachneFight {
    public static final Pos SPAWN = new Pos(-290.5, 47, -190.5);
    public static final int[] ALTAR = {-282, 48, -178};
    static final double ALTAR_REACH = 2.5;
    static final long TIME_LIMIT_MILLIS = 600_000;
    static final int DIALOGUE_TICKS = 20;
    static final int TOP_DAMAGE_RANKS = 5;
    static final int SOUL_QUEEN_RANKS = 3;
    static final double SOUL_QUEEN_RANK_CHANCE = 50;
    static final int CRYSTAL_SOUL_STRING = 12;
    private static final double ARENA_RANGE = 70;

    private static final Map<UUID, Integer> PLACED = new LinkedHashMap<>();
    private static volatile boolean active;
    private static MobArachne boss;
    private static long startedAt;
    private static UUID crystalPlacer;
    private static final Map<UUID, Integer> FIGHT_PLACED = new HashMap<>();
    private static BossBar bar;
    private static Task task;

    private ArachneFight() {
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isAltar(int x, int y, int z) {
        return Math.abs(x - ALTAR[0]) <= ALTAR_REACH && Math.abs(z - ALTAR[2]) <= ALTAR_REACH && Math.abs(y - ALTAR[1]) <= 2;
    }

    public static int placedCallings() {
        return PLACED.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static void place(SkyBlockPlayer player, ArachneTier tier) {
        if (active) {
            player.sendMessage("<c>Arachne is already awake!");
            return;
        }
        if (tier == ArachneTier.GREATER && placedCallings() > 0) {
            player.sendMessage("<c>The altar is holding Arachne's Callings. Finish calling her first.");
            return;
        }
        int needed = tier.itemsRequired();
        if (player.getSkills().getCurrentLevel(SkillCategories.COMBAT) < tier.combatLevel()) {
            player.sendMessage("<c>You need Combat " + tier.combatLevel() + " to call forth Arachne's full strength.");
            return;
        }
        if (!player.removeItemFromPlayer(ItemType.get(tier.summonItem()), 1)) return;
        PLACED.merge(player.getUuid(), 1, Integer::sum);
        int count = placedCallings();
        if (tier == ArachneTier.LESSER && count < needed) {
            broadcast(player.getInstance(), "<5>" + player.getUsername() + " <d>placed an Arachne's Calling! <8>(" + count + "/" + needed + ")");
            return;
        }
        if (tier == ArachneTier.LESSER) {
            broadcast(player.getInstance(), "<5>" + player.getUsername() + " <d>placed the last Arachne's Calling!");
        } else {
            crystalPlacer = player.getUuid();
            broadcast(player.getInstance(), "<5>" + player.getUsername() + " <d>placed an Arachne Crystal!");
        }
        summon(player.getInstance(), tier);
    }

    private static void summon(Instance instance, ArachneTier tier) {
        active = true;
        FIGHT_PLACED.clear();
        FIGHT_PLACED.putAll(PLACED);
        PLACED.clear();
        List<String> lines = tier.spawning();
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            MinecraftServer.getSchedulerManager().buildTask(() -> boss(instance, text))
                    .delay(TaskSchedule.tick((i + 1) * DIALOGUE_TICKS)).schedule();
        }
        MinecraftServer.getSchedulerManager().buildTask(() -> spawn(instance, tier))
                .delay(TaskSchedule.tick((lines.size() + 1) * DIALOGUE_TICKS)).schedule();
    }

    private static void spawn(Instance instance, ArachneTier tier) {
        if (!active) return;
        if (!instance.isChunkLoaded(SPAWN)) instance.loadChunk(SPAWN).join();
        boss = new MobArachne(tier);
        boss.setInstance(instance, SPAWN);
        startedAt = System.currentTimeMillis();
        tier.spawned().forEach(line -> boss(instance, line));
        startBar(instance);
    }

    public static void say(MobArachne mob, List<String> lines) {
        Instance instance = mob.getInstance();
        if (instance == null) return;
        lines.forEach(line -> boss(instance, line));
    }

    private static void boss(Instance instance, String line) {
        broadcast(instance, "<c>[BOSS] Arachne<f>: " + line);
    }

    private static void broadcast(Instance instance, String message) {
        if (instance != null) instance.getPlayers().forEach(player -> player.sendMessage(Text.parse(message).asComponent()));
    }

    private static void startBar(Instance instance) {
        bar = BossBar.bossBar(Text.parse("<c>Arachne").asComponent(), 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        task = MinecraftServer.getSchedulerManager().buildTask(() -> tick(instance)).repeat(TaskSchedule.tick(10)).schedule();
    }

    private static void tick(Instance instance) {
        if (boss == null || boss.isRemoved() || boss.isDead()) {
            reset(instance);
            return;
        }
        if (System.currentTimeMillis() - startedAt > TIME_LIMIT_MILLIS) {
            broadcast(instance, "<c>Arachne is bored with your feeble attempt!");
            boss.clearBrood();
            boss.remove();
            reset(instance);
            return;
        }
        bar.progress(Math.max(0f, Math.min(1f, boss.getHealth() / boss.getMaxHealth())));
        for (Player player : instance.getPlayers()) {
            if (player.getPosition().distance(SPAWN) <= ARENA_RANGE) player.showBossBar(bar);
            else player.hideBossBar(bar);
        }
    }

    private static void reset(Instance instance) {
        if (task != null) task.cancel();
        task = null;
        if (bar != null && instance != null) instance.getPlayers().forEach(player -> player.hideBossBar(bar));
        bar = null;
        boss = null;
        crystalPlacer = null;
        FIGHT_PLACED.clear();
        active = false;
    }

    public static void defeated(MobArachne mob) {
        Instance instance = mob.getInstance();
        ArachneTier tier = mob.tier();
        if (instance != null) tier.defeated().forEach(line -> boss(instance, line));
        List<UUID> ranking = new ArrayList<>();
        mob.getDamageDealt().entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<UUID, Double>comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> ranking.add(entry.getKey()));
        UUID placer = crystalPlacer;
        UUID killer = mob.getLastDamageSource() != null && mob.getLastDamageSource().getAttacker() instanceof SkyBlockPlayer last
                ? last.getUuid() : null;
        Map<UUID, Integer> placed = new HashMap<>(FIGHT_PLACED);
        for (int rank = 0; rank < ranking.size(); rank++) {
            if (!(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(ranking.get(rank)) instanceof SkyBlockPlayer player)) continue;
            reward(player, mob, tier, rank, placed.getOrDefault(player.getUuid(), 0), player.getUuid().equals(placer));
            if (!player.getUuid().equals(killer)) creditBestiary(player, mob);
        }
        if (placer != null && !ranking.contains(placer)
                && MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(placer) instanceof SkyBlockPlayer player) {
            CrimsonMobLoot.roll(player, mob, List.of(always("DARK_QUEENS_SOUL_DROP", 1)));
        }
        reset(instance);
    }

    private static void creditBestiary(SkyBlockPlayer player, MobArachne mob) {
        if (!mob.countsForBestiary()) return;
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class).getValue().increase(mob, 1);
    }

    static void reward(SkyBlockPlayer player, MobArachne mob, ArachneTier tier, int rank, int placed, boolean placedCrystal) {
        Essences.add(player, EssenceType.SPIDER, tier.essence());
        CrimsonMobLoot.roll(player, mob, lootFor(tier, rank, placed, placedCrystal));
        recordFirstKill(player, tier);
    }

    static List<MobDrop> lootFor(ArachneTier tier, int rank, int placed, boolean placedCrystal) {
        boolean top = rank < TOP_DAMAGE_RANKS;
        List<MobDrop> drops = new ArrayList<>();
        if (tier == ArachneTier.LESSER) {
            drops.add(always("ARACHNE_FRAGMENT", 1));
            drops.add(always("LUXURIOUS_SPOOL", 1));
            drops.add(always("STRING", 10, 20));
            drops.add(always("ENCHANTED_STRING", 0, 2));
            drops.add(always("SPIDER_EYE", 10, 20));
            drops.add(always("ENCHANTED_SPIDER_EYE", 0, 2));
            drops.add(top ? always("SOUL_STRING", 5 + placed, 6 + placed) : always("SOUL_STRING", 1 + placed));
            drops.add(chance("SHARD_ARACHNE", 25));
            drops.add(chance("ARACHNE_FANG", 10));
            armor(drops, 10);
            drops.add(pet("TARANTULA_PET", Rarity.EPIC, 2));
        } else {
            int crystals = placedCrystal ? 1 : 0;
            drops.add(always("STRING", 10, 60));
            drops.add(always("ENCHANTED_STRING", 0, 6));
            drops.add(always("SPIDER_EYE", 10, 60));
            drops.add(always("ENCHANTED_SPIDER_EYE", 0, 6));
            int bonus = crystals * CRYSTAL_SOUL_STRING;
            drops.add(top ? always("SOUL_STRING", 30 + bonus, 32 + bonus) : always("SOUL_STRING", 1 + bonus, 3 + bonus));
            if (placedCrystal) drops.add(always("DARK_QUEENS_SOUL_DROP", 1));
            else if (rank < SOUL_QUEEN_RANKS) drops.add(chance("DARK_QUEENS_SOUL_DROP", SOUL_QUEEN_RANK_CHANCE));
            drops.add(chance("ARACK", 25));
            armor(drops, 25);
            drops.add(chance("SHARD_ARACHNE", 25));
            drops.add(pet("TARANTULA_PET", Rarity.LEGENDARY, 2));
        }
        return drops;
    }

    private static void armor(List<MobDrop> drops, double percent) {
        for (String piece : List.of("ARACHNE_HELMET", "ARACHNE_CHESTPLATE", "ARACHNE_LEGGINGS", "ARACHNE_BOOTS")) {
            drops.add(chance(piece, percent));
        }
    }

    public static boolean hasDefeated(SkyBlockPlayer player, ArachneTier tier) {
        return player.getToggles().get(toggle(tier));
    }

    public static boolean hasDefeatedEither(SkyBlockPlayer player) {
        return hasDefeated(player, ArachneTier.LESSER) || hasDefeated(player, ArachneTier.GREATER);
    }

    private static DatapointToggles.Toggles.ToggleType toggle(ArachneTier tier) {
        return tier == ArachneTier.LESSER
                ? DatapointToggles.Toggles.ToggleType.HAS_DEFEATED_ARACHNE_LESSER
                : DatapointToggles.Toggles.ToggleType.HAS_DEFEATED_ARACHNE_GREATER;
    }

    public static void recordFirstKill(SkyBlockPlayer player, ArachneTier tier) {
        player.getToggles().set(toggle(tier), true);
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        double earned = 0;
        for (ArachneTier tier : ArachneTier.values()) if (hasDefeated(player, tier)) earned += tier.firstKillSkyBlockXP();
        var experience = player.getSkyBlockExperience();
        if (earned > experience.getSourceXP(SkyBlockXPSource.DEFEAT_ARACHNE)) experience.setSourceXP(SkyBlockXPSource.DEFEAT_ARACHNE, earned);
    }

    public static void maxOut(SkyBlockPlayer player) {
        for (ArachneTier tier : ArachneTier.values()) player.getToggles().set(toggle(tier), true);
        syncSkyBlockXP(player);
    }

    public static void start() {
        ArachneVenom.start();
    }
}
