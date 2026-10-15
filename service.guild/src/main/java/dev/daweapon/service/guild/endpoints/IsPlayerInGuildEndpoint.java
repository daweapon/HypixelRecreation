package dev.daweapon.service.guild.endpoints;

import dev.daweapon.commons.protocol.objects.guild.IsPlayerInGuildProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.guild.GuildCache;

// Redis endpoint that reports whether a given player is currently in a guild.
public class IsPlayerInGuildEndpoint implements RedisMessageHandler<
    IsPlayerInGuildProtocolObject.IsPlayerInGuildMessage,
    IsPlayerInGuildProtocolObject.IsPlayerInGuildResponse> {

    @Override
    public IsPlayerInGuildProtocolObject protocol() {
        return new IsPlayerInGuildProtocolObject();
    }

    @Override
    public IsPlayerInGuildProtocolObject.IsPlayerInGuildResponse handle(IsPlayerInGuildProtocolObject.IsPlayerInGuildMessage messageObject, RedisMessageContext context) {
        return new IsPlayerInGuildProtocolObject.IsPlayerInGuildResponse(
            GuildCache.isInGuild(messageObject.playerUUID())
        );
    }
}
