package net.swofty.type.generic.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.tinylog.Logger;

/**
 * Minestom removes a disconnected player on that player's next tick, so a player who has stopped
 * ticking - the same condition that gets them kicked for "Too Many Packets" - is never removed,
 * never saved, and blocks every rejoin until the server restarts.
 */
public final class StalePlayerReaper {
    private static final TaskSchedule INTERVAL = TaskSchedule.seconds(2);

    private StalePlayerReaper() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(StalePlayerReaper::sweep)
                .repeat(INTERVAL).schedule();
    }

    private static void sweep() {
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            for (Player player : instance.getPlayers()) {
                if (player.isOnline() || player.isRemoved()) continue;
                Logger.warn("Removing {} ({}): disconnected but never removed", player.getUsername(), player.getUuid());
                player.remove();
            }
        }
    }
}
