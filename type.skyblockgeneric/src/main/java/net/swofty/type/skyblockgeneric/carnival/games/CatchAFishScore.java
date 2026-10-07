package net.swofty.type.skyblockgeneric.carnival.games;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGame;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGameResult;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Catch a Fish scoring: fish points, the Catch Streak Bonus and its double-points chance. */
@Getter
public class CatchAFishScore {
    public static final int SECONDS = 100;
    public static final int STREAK_BONUS_PER_CATCH = 5;
    public static final int MAX_STREAK_BONUS = 50;
    public static final double DOUBLE_POINTS_CHANCE = 0.10;
    public static final int STREAK_TIMEOUT_TICKS = 60;

    /**
     * {@code reelTicks} is how long the reel must be held; the wiki only says red fills fastest
     * and yellow slowest, so these are estimates.
     */
    @Getter
    public enum FishColor {
        RED("Red", "<c>", 50, 60, 20),
        BLUE("Blue", "<9>", 100, 30, 35),
        YELLOW("Yellow", "<e>", 200, 10, 50);

        private final String displayName;
        private final String color;
        private final int points;
        private final int weight;
        private final int reelTicks;

        FishColor(String displayName, String color, int points, int weight, int reelTicks) {
            this.displayName = displayName;
            this.color = color;
            this.points = points;
            this.weight = weight;
            this.reelTicks = reelTicks;
        }

        public static FishColor roll(Random random) {
            int roll = random.nextInt(100);
            for (FishColor color : values()) {
                if ((roll -= color.weight) < 0) return color;
            }
            return RED;
        }
    }

    private long score;
    private int streak;
    private int bestStreak;
    private int fish;
    private int yellowFish;

    public int streakBonus() {
        return Math.min(streak * STREAK_BONUS_PER_CATCH, MAX_STREAK_BONUS);
    }

    /** Scores a catch and returns its points. */
    public int onCatch(FishColor color, boolean doubled) {
        int points = (color.points + streakBonus()) * (doubled ? 2 : 1);
        score += points;
        streak++;
        bestStreak = Math.max(bestStreak, streak);
        fish++;
        if (color == FishColor.YELLOW) yellowFish++;
        return points;
    }

    public void breakStreak() {
        streak = 0;
    }

    public CarnivalGameResult result() {
        Map<Metric, Integer> metrics = new EnumMap<>(Metric.class);
        metrics.put(Metric.FISH, fish);
        metrics.put(Metric.YELLOW_FISH, yellowFish);
        metrics.put(Metric.BEST_STREAK, bestStreak);
        return new CarnivalGameResult(CarnivalGame.CATCH_A_FISH, score, metrics);
    }
}
