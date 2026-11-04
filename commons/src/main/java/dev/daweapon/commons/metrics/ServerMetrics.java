package dev.daweapon.commons.metrics;

import dev.daweapon.commons.ServerType;

public record ServerMetrics(
        String serverUuid,
        String serverName,
        String shortName,
        ServerType type,
        String host,
        double tps,
        double mspt,
        long heapUsedMb,
        long heapMaxMb,
        int players,
        int maxPlayers,
        long publishedAt
) {
    public static final double LOADED_BELOW_TPS = 18;
    public static final double FAILING_BELOW_TPS = 12;
    public static final double LOADED_MSPT = 45;
    public static final double FAILING_MSPT = 100;
    public static final double LOADED_HEAP_RATIO = 0.85;
    public static final double FAILING_HEAP_RATIO = 0.95;
    public static final long STALE_AFTER_MILLIS = 15_000;

    public enum Health {
        NOMINAL,
        LOADED,
        FAILING
    }

    public double heapRatio() {
        return heapMaxMb <= 0 ? 0 : (double) heapUsedMb / heapMaxMb;
    }

    public long ageMillis(long now) {
        return Math.max(0, now - publishedAt);
    }

    public Health health(long now) {
        if (ageMillis(now) > STALE_AFTER_MILLIS || tps < FAILING_BELOW_TPS || mspt >= FAILING_MSPT
                || heapRatio() >= FAILING_HEAP_RATIO) {
            return Health.FAILING;
        }
        if (tps < LOADED_BELOW_TPS || mspt >= LOADED_MSPT || heapRatio() >= LOADED_HEAP_RATIO) {
            return Health.LOADED;
        }
        return Health.NOMINAL;
    }

    public boolean belongsToContainer(String containerId, String containerName) {
        if (host == null || host.isBlank()) return false;
        return host.equals(containerName) || (containerId != null && containerId.startsWith(host));
    }
}
