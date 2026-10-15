package dev.daweapon.type.skyblockgeneric.redis;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.proxy.from.DoesServerHaveGardenProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

public class RedisHasGardenLoaded implements RedisMessageHandler<DoesServerHaveGardenProtocol.Request, DoesServerHaveGardenProtocol.Response> {
    @Override
    public RedisProtocol<DoesServerHaveGardenProtocol.Request, DoesServerHaveGardenProtocol.Response> protocol() {
        return new DoesServerHaveGardenProtocol();
    }

    @Override
    public DoesServerHaveGardenProtocol.Response handle(DoesServerHaveGardenProtocol.Request message, RedisMessageContext context) {
        UUID gardenUUID = UUID.fromString(message.gardenUuid());
        return new DoesServerHaveGardenProtocol.Response(SkyBlockGarden.hasGarden(gardenUUID), true, null);
    }
}
