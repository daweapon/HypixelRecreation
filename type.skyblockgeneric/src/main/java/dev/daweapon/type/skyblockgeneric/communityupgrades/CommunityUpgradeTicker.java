package dev.daweapon.type.skyblockgeneric.communityupgrades;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;

public final class CommunityUpgradeTicker {
    private static boolean started;

    private CommunityUpgradeTicker() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        MinecraftServer.getSchedulerManager().buildTask(CommunityUpgradeTicker::tick)
                .delay(TaskSchedule.seconds(20))
                .repeat(TaskSchedule.seconds(20))
                .schedule();
    }

    private static void tick() {
        for (SkyBlockPlayer player : new ArrayList<>(SkyBlockGenericLoader.getLoadedPlayers())) {
            try {
                CommunityUpgrades.finishDue(player);
            } catch (RuntimeException ignored) {
            }
        }
    }
}
