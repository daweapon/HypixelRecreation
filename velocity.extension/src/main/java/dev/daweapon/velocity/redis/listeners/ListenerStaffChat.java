package dev.daweapon.velocity.redis.listeners;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.from.BroadcastStaffChatProtocol;
import dev.daweapon.commons.protocol.objects.proxy.to.StaffChatProtocol;
import dev.daweapon.velocity.gamemanager.GameManager;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisClient;

import java.util.UUID;

// Redis handler that relays staff chat messages and staff join/leave notifications to every registered backend server.
public class ListenerStaffChat implements RedisMessageHandler<
        StaffChatProtocol.Request,
        StaffChatProtocol.Response> {

    @Override
    public RedisProtocol<StaffChatProtocol.Request, StaffChatProtocol.Response> protocol() {
        return new StaffChatProtocol();
    }

    @Override
    public StaffChatProtocol.Response handle(StaffChatProtocol.Request message, RedisMessageContext context) {
        broadcastToAllServers(new BroadcastStaffChatProtocol.Request(
                message.type(), message.formattedMessage(), message.uuid()));
        return new StaffChatProtocol.Response();
    }

    public static void broadcastToAllServers(BroadcastStaffChatProtocol.Request message) {
        GameManager.getServers().forEach((serverType, serverList) -> {
            serverList.forEach(gameServer -> {
                RedisClient.requestServer(
                        gameServer.internalID(),
                        new BroadcastStaffChatProtocol(),
                        message
                );
            });
        });
    }

    public static void broadcastStaffJoin(UUID playerUuid) {
        broadcastToAllServers(new BroadcastStaffChatProtocol.Request(
                "join", null, playerUuid.toString()));
    }

    public static void broadcastStaffLeave(UUID playerUuid) {
        broadcastToAllServers(new BroadcastStaffChatProtocol.Request(
                "leave", null, playerUuid.toString()));
    }
}
