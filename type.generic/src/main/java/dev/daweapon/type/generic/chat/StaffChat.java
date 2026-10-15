package dev.daweapon.type.generic.chat;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import dev.daweapon.commons.protocol.objects.proxy.to.StaffChatProtocol;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.user.HypixelPlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Sends staff-only chat messages and notifications, broadcast to the network via the proxy.
public final class StaffChat {

    private static final StaffChatProtocol PROTOCOL = new StaffChatProtocol();

    public static void sendMessage(HypixelPlayer sender, String message) {
        Text formatted = Text.of("<b>[STAFF] <rank:{}>{}<f>: {}",
                sender.getRank(), sender.getUsername(), message);
        broadcastViaProxy(formatted.serialize());
    }

    public static void sendNotification(String message) {
        broadcastViaProxy(Text.of("<b>[STAFF] <7>{}", message).serialize());
    }

    private static void broadcastViaProxy(String formattedMessage) {
        RedisClient.requestProxy(PROTOCOL, new StaffChatProtocol.Request("message", formattedMessage, null));
    }
}
