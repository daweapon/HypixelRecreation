package dev.daweapon.type.skyblockgeneric.entity.mob.island;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.island.IslandUtilityBlocks;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.island.VisitManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawns monsters around the players on each loaded Private Island at night.
 *
 * Every island is its own instance, so this walks the loaded islands rather than one shared world.
 * Hypixel spawns in low light; the island dimension here is lit everywhere, so night is the only
 * darkness there is to go by. Species weights are an estimate of the wiki's "rare" Enderman and
 * "very rare" Witch, and the per-island cap is not documented, so it is kept modest.
 */
public final class PrivateIslandMobSpawner {
    private static final int MAX_MOBS_PER_ISLAND = 12;
    private static final int SPAWNS_PER_CYCLE = 2;
    private static final int MIN_DISTANCE = 16;
    private static final int MAX_DISTANCE = 32;
    private static final int DESPAWN_DISTANCE = 64;
    private static final Map<IslandMobSpecies, Integer> WEIGHTS = Map.of(
            IslandMobSpecies.ZOMBIE, 30,
            IslandMobSpecies.SKELETON, 25,
            IslandMobSpecies.SPIDER, 25,
            IslandMobSpecies.CREEPER, 15,
            IslandMobSpecies.ENDERMAN, 4,
            IslandMobSpecies.WITCH, 1);

    private static final Map<UUID, List<PrivateIslandMob>> spawned = new ConcurrentHashMap<>();

    private PrivateIslandMobSpawner() {
    }

    public static void start(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            SkyBlockIsland.loaded().forEach(PrivateIslandMobSpawner::cycle);
            spawned.keySet().removeIf(id -> SkyBlockIsland.loaded().stream().noneMatch(i -> i.getIslandID().equals(id)));
            return TaskSchedule.seconds(5);
        });
    }

    static boolean isNight(long time) {
        long dayTime = Math.floorMod(time, 24000L);
        return dayTime >= 13000 && dayTime < 23000;
    }

    private static void cycle(SkyBlockIsland island) {
        if (!Boolean.TRUE.equals(island.getCreated())) return;
        Instance instance = island.getIslandInstance();
        if (instance == null) return;

        List<PrivateIslandMob> mobs = spawned.computeIfAbsent(island.getIslandID(), _ -> new ArrayList<>());
        mobs.removeIf(mob -> mob.isRemoved() || mob.isDead());

        List<Player> players = new ArrayList<>(instance.getPlayers());
        mobs.removeIf(mob -> {
            boolean farFromEveryone = players.stream()
                    .noneMatch(p -> p.getPosition().distance(mob.getPosition()) < DESPAWN_DISTANCE);
            if (farFromEveryone) mob.remove();
            return farFromEveryone;
        });

        boolean silenced = IslandUtilityBlocks.has(instance, ItemType.SILENCE_BLOCK);
        mobs.forEach(mob -> mob.setSilent(silenced));

        if (players.isEmpty() || !isNight(instance.getTime())) return;
        if (IslandUtilityBlocks.has(instance, ItemType.REPELLING_CANDLE)) return;

        int level = IslandMobSpecies.levelForCombat(ownerCombatLevel(players));
        for (int i = 0; i < SPAWNS_PER_CYCLE && mobs.size() < MAX_MOBS_PER_ISLAND; i++) {
            Player anchor = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            Pos position = findSpawn(instance, anchor.getPosition());
            if (position == null) continue;

            PrivateIslandMob mob = PrivateIslandMob.of(pickSpecies(), level);
            mob.setSilent(silenced);
            mob.setInstance(instance, position);
            mobs.add(mob);
        }
    }

    // Levels follow the island owners' Combat, never a visiting guest's.
    private static int ownerCombatLevel(List<Player> players) {
        return players.stream()
                .filter(p -> p instanceof SkyBlockPlayer && !VisitManager.isVisiting(p.getUuid()))
                .mapToInt(p -> ((SkyBlockPlayer) p).getSkills().getCurrentLevel(SkillCategories.COMBAT))
                .max()
                .orElse(0);
    }

    private static IslandMobSpecies pickSpecies() {
        int total = WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (IslandMobSpecies species : IslandMobSpecies.values()) {
            roll -= WEIGHTS.getOrDefault(species, 0);
            if (roll < 0) return species;
        }
        return IslandMobSpecies.ZOMBIE;
    }

    private static Pos findSpawn(Instance instance, Pos around) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(Math.PI * 2);
        double distance = MIN_DISTANCE + random.nextDouble(MAX_DISTANCE - MIN_DISTANCE);
        int x = (int) Math.floor(around.x() + Math.cos(angle) * distance);
        int z = (int) Math.floor(around.z() + Math.sin(angle) * distance);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) return null;

        int top = (int) around.y() + 16;
        int bottom = Math.max((int) around.y() - 16, instance.getCachedDimensionType().minY());
        for (int y = top; y > bottom; y--) {
            Block ground = instance.getBlock(x, y - 1, z);
            if (ground.isSolid() && !ground.isLiquid()
                    && instance.getBlock(x, y, z).isAir() && instance.getBlock(x, y + 1, z).isAir()) {
                return new Pos(x + 0.5, y, z + 0.5);
            }
        }
        return null;
    }
}
