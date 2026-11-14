package dev.daweapon.service.replay;

import dev.daweapon.service.generic.MongoReadiness;
import lombok.Getter;
import dev.daweapon.commons.ServiceType;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.service.generic.SkyBlockService;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.replay.session.ReplaySessionManager;
import dev.daweapon.service.replay.storage.ReplayDatabase;
import org.tinylog.Logger;

import java.util.List;

// Backend microservice that records, stores, and serves game replays and their maps, backed by MongoDB.
public class ReplayService implements SkyBlockService {

    @Getter
	private static ReplayDatabase database;
    @Getter
	private static ReplaySessionManager sessionManager;

    static void main() {
        MongoReadiness.await(ConfigProvider.settings().getMongodb());
        Logger.info("Starting Replay Service...");

        String mongoUri = ConfigProvider.settings().getMongodb();
        database = new ReplayDatabase();
        database.connect(mongoUri);

        sessionManager = new ReplaySessionManager(database);
        sessionManager.startCleanupTask();

        SkyBlockService.init(new ReplayService());

        Logger.info("Replay Service started successfully");
    }

	@Override
    public ServiceType getType() {
        return ServiceType.REPLAY;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("dev.daweapon.service.replay.endpoints", RedisMessageHandler.class).toList();
    }
}
