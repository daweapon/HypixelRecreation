package dev.daweapon.type.theend.dragon;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;

public final class DragonEgg {
    private static final int PLACE_DELAY_SECONDS = 5;

    private DragonEgg() {
    }

    public static void placeSoon() {
        MinecraftServer.getSchedulerManager().buildTask(() -> place(HypixelConst.getInstanceContainer()))
                .delay(TaskSchedule.seconds(PLACE_DELAY_SECONDS)).schedule();
    }

    public static void place(Instance instance) {
        if (instance == null) return;
        if (!instance.isChunkLoaded(DragonArena.EGG_BLOCK)) instance.loadChunk(DragonArena.EGG_BLOCK).join();
        instance.setBlock(DragonArena.EGG_BLOCK, Block.DRAGON_EGG);
    }

    public static void remove(Instance instance) {
        if (instance == null) return;
        instance.setBlock(DragonArena.EGG_BLOCK, Block.AIR);
    }
}
