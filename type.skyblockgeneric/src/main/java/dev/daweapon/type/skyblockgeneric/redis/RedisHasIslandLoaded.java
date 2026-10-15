package dev.daweapon.type.skyblockgeneric.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.from.DoesServerHaveIslandProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

public class RedisHasIslandLoaded implements RedisMessageHandler<DoesServerHaveIslandProtocol.Request, DoesServerHaveIslandProtocol.Response> {
    @Override
    public RedisProtocol<DoesServerHaveIslandProtocol.Request, DoesServerHaveIslandProtocol.Response> protocol() {
        return new DoesServerHaveIslandProtocol();
    }

    @Override
    public DoesServerHaveIslandProtocol.Response handle(DoesServerHaveIslandProtocol.Request message, RedisMessageContext context) {
        UUID islandUUID = UUID.fromString(message.islandUuid());
        return new DoesServerHaveIslandProtocol.Response(SkyBlockIsland.hasIsland(islandUUID), true, null);
    }
}
