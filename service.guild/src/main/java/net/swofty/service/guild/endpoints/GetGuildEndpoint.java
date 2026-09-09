package net.swofty.service.guild.endpoints;

import net.swofty.commons.protocol.objects.guild.GetGuildProtocolObject;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.service.guild.GuildCache;

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
