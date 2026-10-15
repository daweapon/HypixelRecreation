package dev.daweapon.type.generic.presence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.objects.presence.UpdatePresenceProtocol;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.user.HypixelPlayer;

/**
 * Periodically refreshes player presence to the friend service to avoid stale status.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PresenceHeartbeat {

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(PresenceHeartbeat::pulse)
                .delay(TaskSchedule.seconds(2))
                .repeat(TaskSchedule.seconds(10))
                .schedule();
    }

    private static void pulse() {
        String serverType = null;
        String serverId = null;
        try {
            serverType = HypixelConst.getTypeLoader().getType().name();
            if (HypixelConst.getServerUUID() != null) {
                serverId = HypixelConst.getServerUUID().toString();
            } else if (HypixelConst.getServerName() != null) {
                serverId = HypixelConst.getServerName();
            }
        } catch (Exception ignored) {
        }

        for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
            PresenceInfo info = new PresenceInfo(
                    player.getUuid(),
                    true,
                    serverType,
                    serverId,
                    System.currentTimeMillis()
            );

            RedisClient.publishService(
                    ServiceType.FRIEND,
                    new UpdatePresenceProtocol(),
                    new UpdatePresenceProtocol.UpdatePresenceMessage(info)
            );
        }
    }
}
