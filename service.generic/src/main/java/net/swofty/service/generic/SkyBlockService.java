package net.swofty.service.generic;

import net.swofty.commons.reflection.PackageScanner;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.List;
import java.util.stream.Stream;

// Base contract for a standalone backend microservice: declares its ServiceType and Redis endpoints, and bootstraps via init().
public interface SkyBlockService {
    ServiceType getType();

    List<RedisMessageHandler> getEndpoints();

    default void onReady() {
    }

    static void init(SkyBlockService service) {
        new ServiceInitializer(service).init();
    }

    default <T> Stream<T> loopThroughPackage(String packageName, Class<T> clazz) {
        return PackageScanner.instantiateSubtypes(packageName, clazz);
    }
}
