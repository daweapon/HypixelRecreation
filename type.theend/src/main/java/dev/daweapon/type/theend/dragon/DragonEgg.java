package dev.daweapon.type.theend.dragon;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;

import java.util.ArrayList;
import java.util.List;

public final class DragonEgg {
    private static final int CAPTURE_DELAY_SECONDS = 5;

    private record Saved(Pos at, Block block) {
    }

    private static final List<Saved> BLOCKS = new ArrayList<>();

    private DragonEgg() {
    }

    public static void captureSoon() {
        MinecraftServer.getSchedulerManager().buildTask(() -> capture(HypixelConst.getInstanceContainer()))
                .delay(TaskSchedule.seconds(CAPTURE_DELAY_SECONDS)).schedule();
    }

    public static synchronized void capture(Instance instance) {
        if (instance == null || !BLOCKS.isEmpty()) return;
        for (int x = DragonArena.EGG_MIN.blockX(); x <= DragonArena.EGG_MAX.blockX(); x++) {
            for (int z = DragonArena.EGG_MIN.blockZ(); z <= DragonArena.EGG_MAX.blockZ(); z++) {
                Pos column = new Pos(x, DragonArena.EGG_MIN.blockY(), z);
                if (!instance.isChunkLoaded(column)) instance.loadChunk(column).join();
                for (int y = DragonArena.EGG_MIN.blockY(); y <= DragonArena.EGG_MAX.blockY(); y++) {
                    Block block = instance.getBlock(x, y, z);
                    if (!block.isAir()) BLOCKS.add(new Saved(new Pos(x, y, z), block));
                }
            }
        }
    }

    public static synchronized void remove(Instance instance) {
        if (instance == null) return;
        capture(instance);
        BLOCKS.forEach(saved -> instance.setBlock(saved.at(), Block.AIR));
    }

    public static synchronized void restore(Instance instance) {
        if (instance == null) return;
        BLOCKS.forEach(saved -> instance.setBlock(saved.at(), saved.block()));
    }
}
