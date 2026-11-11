package dev.daweapon.type.skyblockgeneric.carnival.games;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Catch a Fish scoring and reel bar, as captured on Hypixel: fish points plus 5 per catch in the streak from the second
 * catch (capped at 50), sometimes doubled, and a reel bar that fills in a fixed number of two-tick steps per colour.
 */
@Getter
public class CatchAFishScore {
    public static final int SECONDS = 100;
    public static final int STREAK_BONUS_PER_CATCH = 5;
    public static final int MAX_STREAK_BONUS = 50;
    public static final double DOUBLE_POINTS_CHANCE = 0.10;
    public static final int STREAK_TIMEOUT_TICKS = 60;
    public static final int BAR_LENGTH = 48;
    public static final int BAR_UPDATE_TICKS = 2;

    @Getter
    public enum FishColor {
        RED("Red", "<c>", 50, 60, 10, "92d59817c9264185e74387463f226bebfbb15843aa976ab1fc9821136eea39bf"),
        BLUE("Blue", "<9>", 100, 30, 13, "a62ecb1c3fafb766b234591ad595ba196a578787844d27ae6a4c12bc19073297"),
        YELLOW("Yellow", "<e>", 200, 10, 16, "360f4f99c78adddeab276ebdf6ab9a0fcffabd49f0b543e1981ac7bd3d55b18a");

        private final String displayName;
        private final String color;
        private final int points;
        private final int weight;
        private final int reelUpdates;
        private final String texture;

        FishColor(String displayName, String color, int points, int weight, int reelUpdates, String texture) {
            this.displayName = displayName;
            this.color = color;
            this.points = points;
            this.weight = weight;
            this.reelUpdates = reelUpdates;
            this.texture = texture;
        }

        public static FishColor roll(Random random) {
            int roll = random.nextInt(100);
            for (FishColor color : values()) {
                if ((roll -= color.weight) < 0) return color;
            }
            return RED;
        }

        public boolean isReeled(int update) {
            return update >= reelUpdates;
        }

        /** Filled bar characters on the given two-tick update, counted from zero. */
        public int barFilled(int update) {
            return Math.min(BAR_LENGTH, (2 * update + 1) * BAR_LENGTH / (2 * reelUpdates));
        }

        public String barColor(int update) {
            int doubled = 2 * update + 1;
            if (doubled * 4 < 2 * reelUpdates) return "<c>";
            if (doubled * 4 < 6 * reelUpdates) return "<e>";
            return "<a>";
        }

        /** The hi-hat pitch climbing with the bar, in Hypixel's 1/63 steps. */
        public float hatPitch(int update) {
            return ((2 * update + 1) * 63 + 63 * reelUpdates) / (2 * reelUpdates) / 63f;
        }

        public String bar(int update) {
            if (isReeled(update)) return "<a>" + "|".repeat(BAR_LENGTH);
            int filled = barFilled(update);
            return barColor(update) + "|".repeat(filled) + "<8>" + "|".repeat(Math.max(0, BAR_LENGTH - 1 - filled));
        }
    }

    private long score;
    private int streak;
    private int bestStreak;
    private int fish;
    private final Map<FishColor, Integer> caught = new EnumMap<>(FishColor.class);

    public int streakBonus() {
        return streak >= 2 ? Math.min(streak * STREAK_BONUS_PER_CATCH, MAX_STREAK_BONUS) : 0;
    }

    /** Scores a catch and returns its points. */
    public int onCatch(FishColor color, boolean doubled) {
        streak++;
        bestStreak = Math.max(bestStreak, streak);
        int points = (color.points + streakBonus()) * (doubled ? 2 : 1);
        score += points;
        fish++;
        caught.merge(color, 1, Integer::sum);
        return points;
    }

    public void breakStreak() {
        streak = 0;
    }

    public int caught(FishColor color) {
        return caught.getOrDefault(color, 0);
    }

    public Map<FishColor, Integer> getCaught() {
        return Collections.unmodifiableMap(caught);
    }

    public CarnivalGameResult result() {
        Map<Metric, Integer> metrics = new EnumMap<>(Metric.class);
        metrics.put(Metric.FISH, fish);
        metrics.put(Metric.YELLOW_FISH, caught(FishColor.YELLOW));
        metrics.put(Metric.BEST_STREAK, bestStreak);
        return new CarnivalGameResult(CarnivalGame.CATCH_A_FISH, score, metrics);
    }
}
