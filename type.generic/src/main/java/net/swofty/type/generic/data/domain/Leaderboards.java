package net.swofty.type.generic.data.domain;

import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.Datapoint;
import net.swofty.type.generic.leaderboard.LeaderboardService;
import net.swofty.type.generic.leaderboard.LeaderboardTracked;
import net.swofty.type.generic.leaderboard.MapLeaderboardTracked;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class Leaderboards {

    private Leaderboards() {}

    public static void sync(UUID uuid, DataHandler handler) {
        if (!LeaderboardService.isInitialized()) return;

        Map<String, Double> scores = new LinkedHashMap<>();
        for (Datapoint<?> datapoint : handler.getDatapoints().values()) {
            if (datapoint instanceof LeaderboardTracked tracked) {
                String key = tracked.getLeaderboardKey();
                if (key != null) {
                    scores.put(key, tracked.getLeaderboardScore());
                }
            }

            if (datapoint instanceof MapLeaderboardTracked mapTracked) {
                for (Map.Entry<String, Double> entry : mapTracked.getAllLeaderboardScores().entrySet()) {
                    scores.put(mapTracked.getLeaderboardKeyFor(entry.getKey()), entry.getValue());
                }
            }
        }
        LeaderboardService.updateScores(uuid, scores);
    }
}
