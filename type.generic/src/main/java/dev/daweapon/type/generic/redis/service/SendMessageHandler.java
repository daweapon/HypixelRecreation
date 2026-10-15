package dev.daweapon.type.generic.redis.service;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.messaging.SendMessagePushProtocol;
import dev.daweapon.commons.protocol.objects.messaging.SendMessagePushProtocol.Request;
import dev.daweapon.commons.protocol.objects.messaging.SendMessagePushProtocol.Response;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.HypixelGenericLoader;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.redis.RedisMessageContext;

public class SendMessageHandler implements RedisMessageHandler<Request, Response> {

    private static final SendMessagePushProtocol PROTOCOL = new SendMessagePushProtocol();

    @Override
    public RedisProtocol<Request, Response> protocol() {
        return PROTOCOL;
    }

    @Override
    public Response handle(Request message, RedisMessageContext context) {
        try {
            HypixelPlayer player = HypixelGenericLoader.getLoadedPlayers().stream()
                    .filter(p -> p.getUuid().equals(message.playerUUID()))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                String raw = message.message();
                player.sendMessage(raw);
                return new Response(true, null);
            }

            return new Response(false, "Player not found or error");
        } catch (Exception e) {
            return new Response(false, "Player not found or error");
        }
    }
}
