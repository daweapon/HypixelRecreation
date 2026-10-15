package dev.daweapon.service.orchestrator;

import dev.daweapon.commons.ServiceType;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Backend microservice that matchmakes players into game servers; periodically cleans up stale server/game state.
public class OrchestratorService implements SkyBlockService {

	static void main() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "orchestrator-cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(OrchestratorCache::cleanup, 5, 5, TimeUnit.SECONDS);
		Logger.info("Started orchestrator service");

        SkyBlockService.init(new OrchestratorService());
	}

	@Override
	public ServiceType getType() {
		return ServiceType.ORCHESTRATOR;
	}

	@Override
	public List<RedisMessageHandler> getEndpoints() {
		return loopThroughPackage("dev.daweapon.service.orchestrator.endpoints", RedisMessageHandler.class).toList();
	}
}