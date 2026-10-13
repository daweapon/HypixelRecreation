package net.swofty.type.skyblockgeneric.redis;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.from.DoesServerHaveGardenProtocol;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.skyblockgeneric.user.garden.SkyBlockGarden;

import java.util.UUID;
import net.swofty.commons.redis.RedisMessageContext;

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
