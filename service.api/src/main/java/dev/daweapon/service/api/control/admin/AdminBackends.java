package dev.daweapon.service.api.control.admin;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dev.daweapon.commons.config.ConfigProvider;
import dev.daweapon.commons.punishment.PunishmentRedis;
import dev.daweapon.type.generic.data.mongodb.MongoConnection;
import dev.daweapon.type.generic.leaderboard.LeaderboardService;

// The game-data helpers the admin panels reuse (wipe snapshots, leaderboards, punishments) each
// expect their own connection, which game servers open at boot and service.api never did.
public final class AdminBackends {
    private static volatile boolean ready;

    private AdminBackends() {}

    public static synchronized void ensureReady() {
        if (ready) return;
        if (MongoConnection.client() == null) {
            MongoConnection.connect(MongoClients.create(ConfigProvider.settings().getMongodb()));
        }
        String redisUri = ConfigProvider.settings().getRedisUri();
        if (!LeaderboardService.isInitialized()) LeaderboardService.connect(redisUri);
        if (!PunishmentRedis.isInitialized()) PunishmentRedis.connect(redisUri);
        ready = true;
    }

    public static MongoDatabase database() {
        ensureReady();
        return MongoConnection.client().getDatabase(MongoConnection.DATABASE);
    }
}
