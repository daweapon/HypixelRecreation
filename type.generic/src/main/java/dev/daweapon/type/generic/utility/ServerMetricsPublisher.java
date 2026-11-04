package dev.daweapon.type.generic.utility;

import dev.daweapon.commons.metrics.ServerMetrics;
import dev.daweapon.commons.metrics.ServerMetricsStore;
import dev.daweapon.type.generic.HypixelConst;
import net.minestom.server.MinecraftServer;
import org.tinylog.Logger;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServerMetricsPublisher {
    private static final long INTERVAL_SECONDS = 5;
    private static final long BYTES_PER_MB = 1024 * 1024;
    private static boolean started;
    private static String host;

    private ServerMetricsPublisher() {
    }

    public static synchronized void start() {
        if (started) return;
        started = true;
        host = resolveHost();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "server-metrics-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(ServerMetricsPublisher::publish, INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private static void publish() {
        try {
            UUID uuid = HypixelConst.getServerUUID();
            if (uuid == null || HypixelConst.getTypeLoader() == null) return;
            Runtime runtime = Runtime.getRuntime();
            ServerMetricsStore.publish(new ServerMetrics(
                    uuid.toString(),
                    HypixelConst.getServerName(),
                    HypixelConst.getShortenedServerName(),
                    HypixelConst.getTypeLoader().getType(),
                    host,
                    TpsMeter.tps(),
                    Math.round(TpsMeter.averageTickMillis() * 10) / 10.0,
                    (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MB,
                    runtime.maxMemory() == Long.MAX_VALUE ? 0 : runtime.maxMemory() / BYTES_PER_MB,
                    MinecraftServer.getConnectionManager().getOnlinePlayers().size(),
                    HypixelConst.getMaxPlayers(),
                    System.currentTimeMillis()));
        } catch (Exception exception) {
            Logger.debug("Server metrics heartbeat skipped: {}", exception.getMessage());
        }
    }

    private static String resolveHost() {
        String fromEnvironment = System.getenv("HOSTNAME");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) return fromEnvironment;
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "";
        }
    }
}
