package dev.daweapon.type.skyblockgeneric.foraging;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

public final class PrecisionCutting {
    private PrecisionCutting() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(() ->
                        SkyBlockGenericLoader.forEachLoadedPlayer(ForagingTreeManager::showPrecisionMarks))
                .repeat(TaskSchedule.tick(20))
                .schedule();
    }
}
