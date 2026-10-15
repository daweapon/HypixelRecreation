package dev.daweapon.velocity.presence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.objects.presence.UpdatePresenceProtocol;
import dev.daweapon.commons.redis.RedisClient;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Publishes player online/offline presence updates to all backend services over Redis.
public final class PresencePublisher {

    public static void publish(Player player, boolean online, String serverType, UUID serverId) {
        PresenceInfo info = new PresenceInfo(
                player.getUniqueId(),
                online,
                serverType,
                serverId != null ? serverId.toString() : null,
                System.currentTimeMillis()
        );

        RedisClient.publishAllServices(
                new UpdatePresenceProtocol(),
                new UpdatePresenceProtocol.UpdatePresenceMessage(info)
        );
    }

    public static void publish(Player player, boolean online, RegisteredServer server, String serverType) {
        UUID serverId = null;
        if (server != null) {
            try {
                serverId = UUID.fromString(server.getServerInfo().getName());
            } catch (Exception ignored) {
            }
        }
        publish(player, online, serverType, serverId);
    }
}
