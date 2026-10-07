package net.swofty.type.skyblockgeneric.carnival;

import java.util.EnumMap;
import java.util.Map;

public record CarnivalGameResult(CarnivalGame game, long score, Map<Metric, Integer> metrics) {

    public enum Metric {
        APPLES, UNIQUE_FRUITS, DRAGONFRUITS,
        FISH, YELLOW_FISH, BEST_STREAK,
        ZOMBIES, DIAMOND_ZOMBIES, LAMPS
    }

    public CarnivalGameResult {
        metrics = new EnumMap<>(metrics.isEmpty() ? new EnumMap<>(Metric.class) : metrics);
    }

    public int metric(Metric metric) {
        return metrics.getOrDefault(metric, 0);
    }
}
