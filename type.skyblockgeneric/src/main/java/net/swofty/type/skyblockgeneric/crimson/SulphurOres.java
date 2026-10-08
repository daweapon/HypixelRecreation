package net.swofty.type.skyblockgeneric.crimson;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoExplosivity;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sulphur Ore (a sponge block) scattered over the island when the Blazing Volcano erupts. How many
 * land per eruption isn't documented, so the counts scale with explosivity (estimate).
 */
public final class SulphurOres {
    private static final int MIN_X = -768, MAX_X = 47, MIN_Z = -1104, MAX_Z = -401;
    private static final int TOP_Y = 200, BOTTOM_Y = 40;
    private static final int MAX_ACTIVE = 400;
    private static final int ATTEMPTS_PER_ORE = 12;
    private static final Set<Block> GROUND = Set.of(Block.NETHERRACK, Block.RED_SAND, Block.MYCELIUM, Block.SOUL_SAND,
            Block.SOUL_SOIL, Block.BASALT, Block.BLACKSTONE, Block.MAGMA_BLOCK, Block.GRAVEL);
    private static final Map<VolcanoExplosivity, Integer> ORES_PER_ERUPTION = Map.of(
            VolcanoExplosivity.CHILL, 20,
            VolcanoExplosivity.LOW, 30,
            VolcanoExplosivity.DISRUPTIVE, 40,
            VolcanoExplosivity.MEDIUM, 60,
            VolcanoExplosivity.HIGH, 90,
            VolcanoExplosivity.EXPLOSIVE, 130,
            VolcanoExplosivity.CATACLYSMIC, 200);
    private static final Deque<Pos> ACTIVE = new ArrayDeque<>();

    private SulphurOres() {
    }

    public static int countFor(VolcanoExplosivity explosivity) {
        return ORES_PER_ERUPTION.getOrDefault(explosivity, 20);
    }

    public static synchronized void scatter(Instance instance, int wanted) {
        int placed = 0;
        for (int attempt = 0; attempt < wanted * ATTEMPTS_PER_ORE && placed < wanted; attempt++) {
            Pos spot = findSpot(instance);
            if (spot == null) continue;
            instance.setBlock(spot, Block.SPONGE);
            ACTIVE.addLast(spot);
            placed++;
            while (ACTIVE.size() > MAX_ACTIVE) {
                Pos oldest = ACTIVE.removeFirst();
                if (instance.getBlock(oldest).compare(Block.SPONGE)) instance.setBlock(oldest, Block.AIR);
            }
        }
    }

    private static Pos findSpot(Instance instance) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(MIN_X, MAX_X + 1);
        int z = random.nextInt(MIN_Z, MAX_Z + 1);
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) return null;
        for (int y = TOP_Y; y > BOTTOM_Y; y--) {
            Block block = instance.getBlock(x, y, z);
            if (block.isAir()) continue;
            if (!GROUND.contains(block.defaultState())) return null;
            Pos spot = new Pos(x, y + 1, z);
            if (!instance.getBlock(spot).isAir() || !instance.getBlock(spot.add(0, 1, 0)).isAir()) return null;
            SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(spot);
            if (region == null || region.getType().getMiningHandler() == null) return null;
            return spot;
        }
        return null;
    }
}
