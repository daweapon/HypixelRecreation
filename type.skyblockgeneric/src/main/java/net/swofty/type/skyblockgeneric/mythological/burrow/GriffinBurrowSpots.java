package net.swofty.type.skyblockgeneric.mythological.burrow;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.random.RandomGenerator;

/**
 * Where Griffin Burrows may appear. Hypixel only places burrows on grass blocks in the open Hub, so
 * the pool is built by sampling random columns across the Hub's region boxes, keeping the top block
 * when it is grass with headroom above it and the column sits in an open-air region rather than a
 * building or a cave.
 */
public final class GriffinBurrowSpots {
    private static final Set<RegionType> OPEN_GROUND = EnumSet.of(
            RegionType.VILLAGE, RegionType.WILDERNESS, RegionType.RUINS, RegionType.GRAVEYARD,
            RegionType.FOREST, RegionType.FARM, RegionType.FISHING_OUTPOST, RegionType.MOUNTAIN,
            RegionType.UNINCORPORATED, RegionType.COAL_MINE, RegionType.COMBAT_SETTLEMENT);
    private static final int TARGET_SPOTS = 600;
    private static final int MAX_SAMPLES = 25_000;
    private static final int READY_THRESHOLD = 40;

    private static final List<BlockVec> SPOTS = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SAMPLING = new AtomicBoolean();

    private GriffinBurrowSpots() {
    }

    public static boolean isReady() {
        return SPOTS.size() >= READY_THRESHOLD;
    }

    public static void ensureSampled(Instance instance) {
        if (!SAMPLING.compareAndSet(false, true)) return;
        Thread.startVirtualThread(() -> {
            try {
                sample(instance);
                Logger.info("Located {} Griffin Burrow spots in the Hub", SPOTS.size());
            } catch (Exception exception) {
                Logger.error(exception, "Failed to locate Griffin Burrow spots");
            }
        });
    }

    public static Optional<BlockVec> near(Point origin, double minDistance, double maxDistance, RandomGenerator random) {
        List<BlockVec> ring = SPOTS.stream()
                .filter(spot -> {
                    double distance = horizontalDistance(spot, origin);
                    return distance >= minDistance && distance <= maxDistance;
                })
                .toList();
        if (!ring.isEmpty()) return Optional.of(ring.get(random.nextInt(ring.size())));
        return SPOTS.stream()
                .filter(spot -> horizontalDistance(spot, origin) >= minDistance)
                .min(Comparator.comparingDouble(spot -> horizontalDistance(spot, origin)));
    }

    private static void sample(Instance instance) {
        List<SkyBlockRegion> regions = SkyBlockRegion.getRegions().stream()
                .filter(region -> region.getType() != RegionType.PRIVATE_ISLAND)
                .toList();
        if (regions.isEmpty()) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (SkyBlockRegion region : regions) {
            List<Integer> bounds = region.getBounds();
            minX = Math.min(minX, bounds.get(0));
            maxX = Math.max(maxX, bounds.get(1));
            minY = Math.min(minY, bounds.get(2));
            maxY = Math.max(maxY, bounds.get(3));
            minZ = Math.min(minZ, bounds.get(4));
            maxZ = Math.max(maxZ, bounds.get(5));
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < MAX_SAMPLES && SPOTS.size() < TARGET_SPOTS; i++) {
            int x = random.nextInt(minX, maxX + 1);
            int z = random.nextInt(minZ, maxZ + 1);
            surface(instance, x, z, maxY, minY)
                    .filter(GriffinBurrowSpots::isOpenGround)
                    .ifPresent(SPOTS::add);
        }
    }

    private static Optional<BlockVec> surface(Instance instance, int x, int z, int top, int bottom) {
        instance.loadChunk(x >> 4, z >> 4).join();
        for (int y = top; y >= bottom; y--) {
            Block block = instance.getBlock(x, y, z);
            if (block.air() || (!block.solid() && !block.liquid())) continue;
            if (!block.compare(Block.GRASS_BLOCK)) return Optional.empty();
            if (instance.getBlock(x, y + 1, z).solid() || instance.getBlock(x, y + 2, z).solid()) {
                return Optional.empty();
            }
            return Optional.of(new BlockVec(x, y, z));
        }
        return Optional.empty();
    }

    private static boolean isOpenGround(BlockVec spot) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(new Pos(spot.x() + 0.5, spot.y() + 1, spot.z() + 0.5));
        return region != null && OPEN_GROUND.contains(region.getType());
    }

    private static double horizontalDistance(Point a, Point b) {
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
