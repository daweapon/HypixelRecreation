package dev.daweapon.type.theend.dragon;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.generic.HypixelConst;

public final class AltarBuilder {
    private static final int BUILD_DELAY_SECONDS = 5;

    private AltarBuilder() {
    }

    public static void buildSoon() {
        MinecraftServer.getSchedulerManager().buildTask(AltarBuilder::build)
                .delay(TaskSchedule.seconds(BUILD_DELAY_SECONDS)).schedule();
    }

    public static void build() {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null) return;
        for (Pos frame : DragonArena.ALTAR_FRAMES) {
            if (!instance.isChunkLoaded(frame)) instance.loadChunk(frame).join();
            instance.setBlock(frame, Block.END_PORTAL_FRAME
                    .withProperty("eye", "false")
                    .withProperty("facing", DragonArena.frameFacing(frame)));
        }
    }
}
