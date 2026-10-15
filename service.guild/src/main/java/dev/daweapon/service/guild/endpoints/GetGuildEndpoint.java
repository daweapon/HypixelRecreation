package dev.daweapon.service.guild.endpoints;

import dev.daweapon.commons.protocol.objects.guild.GetGuildProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.guild.GuildCache;

// Redis endpoint that looks up the guild a given player belongs to, if any.
public class GetGuildEndpoint implements RedisMessageHandler<
    GetGuildProtocolObject.GetGuildMessage,
    GetGuildProtocolObject.GetGuildResponse> {

    @Override
    public GetGuildProtocolObject protocol() {
        return new GetGuildProtocolObject();
    }

    @Override
    public GetGuildProtocolObject.GetGuildResponse handle(GetGuildProtocolObject.GetGuildMessage messageObject, RedisMessageContext context) {
        return new GetGuildProtocolObject.GetGuildResponse(
            GuildCache.getGuildFromPlayer(messageObject.memberUUID())
        );
    }
}
