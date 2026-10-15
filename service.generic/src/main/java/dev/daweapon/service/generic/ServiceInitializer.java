package dev.daweapon.service.generic;

import lombok.RequiredArgsConstructor;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.redis.RedisChannels;
import dev.daweapon.commons.redis.RedisClient;
import dev.daweapon.commons.redis.RedisEndpoint;
import dev.daweapon.commons.redis.RedisEnvelope;
import dev.daweapon.commons.redis.RedisMessageBus;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisSubscriber;
import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import dev.daweapon.redisapi.api.RedisAPI;
import dev.daweapon.service.generic.redis.PingEndpoint;
import dev.daweapon.service.generic.redis.ServiceRedisManager;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RequiredArgsConstructor
// Bootstraps a SkyBlockService: connects Redis, registers its endpoints (plus a built-in ping endpoint), and blocks until shutdown.
public class ServiceInitializer {
    private final SkyBlockService service;

    public void init() {
        Logger.info("Initializing service {}...", service.getType().name());
        ItemAttribute.registerItemAttributes();

        ServiceRedisManager.connect(ConfigProvider.settings().getRedisUri(), service.getType());
        RedisClient.registerResponseChannel(RedisChannels.SERVICE_RESPONSE);
        RedisClient.registerResponseChannel(RedisChannels.SERVICE_BROADCAST_RESPONSE);

        List<RedisMessageHandler> endpoints = new ArrayList<>(service.getEndpoints());
        endpoints.add(new PingEndpoint());

        endpoints.forEach(endpoint -> {
            RedisProtocol<?, ?> protocolObject = endpoint.protocol();
            Logger.debug("Registering channel {}", protocolObject.channel());

            RedisMessageBus.registerHandler(
                RedisEndpoint.service(service.getType()),
                RedisChannels.protocol(protocolObject),
                endpoint,
                (envelope, _) -> RedisMessageContext.between(
                    UUID.fromString(envelope.id()),
                    RedisEndpoint.server(envelope.from()),
                    RedisEndpoint.service(service.getType()),
                    protocolObject.channel()
                ),
                RedisEnvelope::from,
                _ -> RedisChannels.protocol(protocolObject)
            );
        });

        RedisSubscriber.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                RedisAPI.getInstance().shutdown();
            } catch (Exception exception) {
                Logger.error(exception, "Failed to shut down Redis for service {}", service.getType().name());
            }
        }, "service-redis-shutdown"));

        Logger.info("Service {} initialized!", service.getType().name());
        service.onReady();

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
