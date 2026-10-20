package dev.daweapon.type.theend.dragon;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;

import java.util.ArrayList;
import java.util.List;

public final class DragonGlass {
    public static final int COLUMN_X = -671;
    public static final int COLUMN_Z = -276;
    private static final int BOTTOM_Y = 10;
    private static final int TOP_Y = 46;
    private static final int CAPTURE_DELAY_SECONDS = 5;

    private record Saved(Pos at, Block block) {
    }

    private static final List<Saved> BLOCKS = new ArrayList<>();

    private DragonGlass() {
    }

    public static void captureSoon() {
        MinecraftServer.getSchedulerManager().buildTask(() -> capture(HypixelConst.getInstanceContainer()))
                .delay(TaskSchedule.seconds(CAPTURE_DELAY_SECONDS)).schedule();
    }

    public static synchronized void capture(Instance instance) {
        if (instance == null || !BLOCKS.isEmpty()) return;
        Pos column = new Pos(COLUMN_X, BOTTOM_Y, COLUMN_Z);
        if (!instance.isChunkLoaded(column)) instance.loadChunk(column).join();
        for (int y = BOTTOM_Y; y <= TOP_Y; y++) {
            Block block = instance.getBlock(COLUMN_X, y, COLUMN_Z);
            if (block.name().contains("stained_glass")) BLOCKS.add(new Saved(new Pos(COLUMN_X, y, COLUMN_Z), block));
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
