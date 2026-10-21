package dev.daweapon.type.skyblockgeneric.hunting.creature;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.hunting.creature.trigger.CreatureTriggers;
import dev.daweapon.type.skyblockgeneric.region.MobFreeZones;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Keeps each creature of the island at its population near players: fixed wiki spawn points when it has any, else
 * random surface (or water surface) positions inside its regions. Creatures with a spawn trigger are not
 * populated here; their trigger calls {@link #spawnAt}.
 */
public final class CreatureSpawner {
    public static final double SPAWN_RANGE = 48;
    public static final double DESPAWN_RANGE = 110;
    private static final double MIN_PLAYER_DISTANCE = 10;
    private static final int ATTEMPTS = 8;
    private static final int PER_PASS = 2;
    private static final Map<String, List<HuntingCreatureMob>> ALIVE = new HashMap<>();
    private static boolean started;

    private CreatureSpawner() {
    }

    public static String islandOf(ServerType type) {
        return type.name().replaceFirst("^SKYBLOCK_", "");
    }

    public static void start() {
        if (started || HypixelConst.getTypeLoader() == null) return;
        String island = islandOf(HypixelConst.getTypeLoader().getType());
        List<CreatureSpec> specs = CreatureCatalog.forIsland(island);
        if (specs.isEmpty()) return;
        started = true;
        org.tinylog.Logger.info("Creature spawner started for {} with {} creatures", island, specs.size());
        CreatureTriggers.start();
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            try {
                pass(specs);
            } catch (RuntimeException exception) {
                org.tinylog.Logger.error(exception, "Creature spawner pass failed");
            }
            return TaskSchedule.seconds(2);
        });
    }

    private static void pass(List<CreatureSpec> specs) {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null) return;
        Collection<SkyBlockPlayer> players = SkyBlockGenericLoader.getLoadedPlayers();
        for (CreatureSpec spec : specs) {
            List<HuntingCreatureMob> alive = ALIVE.computeIfAbsent(spec.id(), id -> new ArrayList<>());
            alive.removeIf(mob -> mob.isDead() || mob.isRemoved());
            alive.removeIf(mob -> {
                if (players.stream().anyMatch(player -> player.getInstance() == mob.getInstance()
                        && player.getPosition().distance(mob.getPosition()) < DESPAWN_RANGE)) return false;
                mob.remove();
                return true;
            });
            if (players.isEmpty() || spec.spawn().trigger() != null) continue;
            for (int i = 0; i < PER_PASS && alive.size() < spec.spawn().max(); i++) {
                spawnNear(spec, instance, players);
            }
        }
    }

    public static List<HuntingCreatureMob> alive(String id) {
        return List.copyOf(ALIVE.getOrDefault(id, List.of()));
    }

    public static int aliveCount(String id) {
        return ALIVE.getOrDefault(id, List.of()).size();
    }

    public static HuntingCreatureMob spawnAt(CreatureSpec spec, Instance instance, Pos position) {
        HuntingCreatureMob mob = HuntingCreatureMob.create(spec);
        mob.setInstance(instance, position);
        ALIVE.computeIfAbsent(spec.id(), id -> new ArrayList<>()).add(mob);
        return mob;
    }

    private static Optional<HuntingCreatureMob> spawnNear(CreatureSpec spec, Instance instance, Collection<SkyBlockPlayer> players) {
        List<SkyBlockPlayer> list = new ArrayList<>(players);
        SkyBlockPlayer anchor = list.get(ThreadLocalRandom.current().nextInt(list.size()));
        Pos position = spec.spawn().points().isEmpty() ? randomPosition(spec, instance, anchor) : fixedPoint(spec, anchor);
        if (position == null) return Optional.empty();
        if (list.stream().anyMatch(player -> player.getPosition().distance(position) < MIN_PLAYER_DISTANCE)) return Optional.empty();
        if (MobFreeZones.isMobFreeAt(position)) return Optional.empty();
        return Optional.of(spawnAt(CreatureTriggers.replacement(spec, anchor.getUuid()), instance, position));
    }

    private static @Nullable Pos fixedPoint(CreatureSpec spec, SkyBlockPlayer anchor) {
        List<Pos> free = spec.spawn().points().stream()
                .filter(point -> point.distance(anchor.getPosition()) <= SPAWN_RANGE)
                .filter(point -> alive(spec.id()).stream().noneMatch(mob -> mob.getPosition().distance(point) < 3))
                .toList();
        return free.isEmpty() ? null : free.get(ThreadLocalRandom.current().nextInt(free.size()));
    }

    private static @Nullable Pos randomPosition(CreatureSpec spec, Instance instance, SkyBlockPlayer anchor) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<SkyBlockRegion> regions = SkyBlockRegion.getRegions().stream()
                .filter(region -> spec.spawn().regions().contains(region.getType())).toList();
        if (regions.isEmpty()) return null;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double x = anchor.getPosition().x() + random.nextDouble(-SPAWN_RANGE, SPAWN_RANGE);
            double z = anchor.getPosition().z() + random.nextDouble(-SPAWN_RANGE, SPAWN_RANGE);
            if (!instance.isChunkLoaded((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4)) continue;
            Pos surface = surface(instance, (int) Math.floor(x), (int) Math.floor(z), spec.medium() == CreatureSpec.Medium.WATER,
                    (int) anchor.getPosition().y() + 24);
            if (surface == null) continue;
            if (regions.stream().anyMatch(region -> region.insideRegion(surface))) {
                return spec.medium() == CreatureSpec.Medium.AIR ? surface.add(0, 3, 0) : surface;
            }
        }
        return null;
    }

    public static @Nullable Pos surface(Instance instance, int x, int z, boolean water, int startY) {
        for (int y = startY; y > startY - 64; y--) {
            Block block = instance.getBlock(x, y, z);
            if (block.isAir() || block.name().contains("leaves")) continue;
            boolean liquid = block.isLiquid();
            if (water) {
                return liquid && instance.getBlock(x, y + 1, z).isAir() ? new Pos(x + 0.5, y + 0.1, z + 0.5) : null;
            }
            if (liquid || !block.isSolid()) return null;
            if (instance.getBlock(x, y + 1, z).isAir() && instance.getBlock(x, y + 2, z).isAir()) {
                return new Pos(x + 0.5, y + 1, z + 0.5);
            }
            return null;
        }
        return null;
    }
}
