package net.swofty.type.skyblockgeneric.region.mining;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class MiningSpread {
    private static final ThreadLocal<Boolean> SPREADING = ThreadLocal.withInitial(() -> false);
    private static final List<Vec> NEIGHBOURS = neighbourOffsets();

    private MiningSpread() {
    }

    public static ItemStatistic statisticFor(MineableBlock mineable) {
        if (mineable == null) return null;
        ItemStatistic specific = mineable.getBlockType().specificBlockFortune();
        if (specific == ItemStatistic.GEMSTONE_FORTUNE) return ItemStatistic.GEMSTONE_SPREAD;
        if (specific == ItemStatistic.ORE_FORTUNE || specific == ItemStatistic.BLOCK_FORTUNE
                || specific == ItemStatistic.DWARVEN_METAL_FORTUNE) return ItemStatistic.MINING_SPREAD;
        return null;
    }

    public static int extraBlocks(double spread, double roll) {
        if (spread <= 0) return 0;
        int guaranteed = (int) Math.floor(spread / 100);
        double fraction = spread - guaranteed * 100;
        return guaranteed + (roll * 100 < fraction ? 1 : 0);
    }

    public static List<Vec> neighbourOffsets() {
        List<Vec> offsets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) offsets.add(new Vec(x, y, z));
                }
            }
        }
        return List.copyOf(offsets);
    }

    public static List<Point> candidates(Instance instance, Point origin, Block broken) {
        List<Point> found = new ArrayList<>();
        for (Vec offset : NEIGHBOURS) {
            Point point = origin.add(offset);
            if (instance.getBlock(point).compare(broken)) found.add(point);
        }
        return found;
    }

    public static Point pick(List<Point> candidates, RandomGenerator random) {
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    public static void trigger(SkyBlockPlayer player, Point origin, Block broken, RegionType region) {
        if (SPREADING.get() || player.getInstance() == null) return;
        MineableBlock mineable = MineableBlock.get(broken, region);
        ItemStatistic statistic = statisticFor(mineable);
        if (statistic == null) return;
        double spread = player.getStatistics().allStatistics().getOverall(statistic);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int extra = extraBlocks(spread, random.nextDouble());
        if (extra <= 0) return;

        Instance instance = player.getInstance();
        SPREADING.set(true);
        try {
            Point cursor = origin;
            for (int i = 0; i < extra; i++) {
                Point target = pick(candidates(instance, cursor, broken), random);
                if (target == null) {
                    target = pick(candidates(instance, origin, broken), random);
                    if (target == null) break;
                }
                MinecraftServer.getGlobalEventHandler().call(new PlayerBlockBreakEvent(
                        player, instance, instance.getBlock(target), Block.AIR, target.asBlockVec(), BlockFace.BOTTOM));
                cursor = target;
            }
        } finally {
            SPREADING.set(false);
        }
    }
}
