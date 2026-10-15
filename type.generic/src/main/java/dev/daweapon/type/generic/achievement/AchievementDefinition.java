package dev.daweapon.type.generic.achievement;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Immutable metadata for a single achievement, loaded from configuration (goal, points, tiers, trigger, etc.).
@Getter
@Builder
public class AchievementDefinition {
    private final String id;
    private final String name;
    private final String description;
    private final AchievementType type;
    private final AchievementCategory category;
    private final SeasonType season;
    private final String trigger;
    private final int goal;
    private final int points;
    private final List<AchievementTier> tiers;
    private final boolean perGame;
    private final boolean customCheck;
    private final boolean secret;
    private final boolean legacy;

    public static final String COOP_PREFIX = "[Co-op or you]";
    private static final String VALUE_PLACEHOLDER = "%%value%%";

    public boolean isCoop() {
        return description != null && description.startsWith(COOP_PREFIX);
    }

    public boolean countsTowardsCompletion() {
        return type != AchievementType.SEASONAL && !legacy;
    }

    public String getDescriptionForTier(int tier) {
        if (type != AchievementType.TIERED || description == null) return description;
        return description.replace(VALUE_PLACEHOLDER, String.format("%,d", getGoalForTier(Math.max(1, tier))));
    }

    public int getUnlockableCount() {
        return type == AchievementType.TIERED ? getMaxTier() : 1;
    }

    public int getTotalPoints() {
        if (type == AchievementType.TIERED && tiers != null) {
            return tiers.stream().mapToInt(AchievementTier::points).sum();
        }
        return points;
    }

    public int getGoalForTier(int tier) {
        if (type != AchievementType.TIERED || tiers == null) return goal;
        return tiers.stream()
                .filter(t -> t.tier() == tier)
                .findFirst()
                .map(AchievementTier::goal)
                .orElse(0);
    }

    public int getPointsForTier(int tier) {
        if (type != AchievementType.TIERED || tiers == null) return points;
        return tiers.stream()
                .filter(t -> t.tier() == tier)
                .findFirst()
                .map(AchievementTier::points)
                .orElse(0);
    }

    public int getMaxTier() {
        if (type != AchievementType.TIERED || tiers == null) return 1;
        return tiers.stream().mapToInt(AchievementTier::tier).max().orElse(1);
    }

    public int getPointsUpToTier(int tier) {
        if (type != AchievementType.TIERED || tiers == null) {
            return tier >= 1 ? points : 0;
        }
        return tiers.stream()
                .filter(t -> t.tier() <= tier)
                .mapToInt(AchievementTier::points)
                .sum();
    }

    public boolean isForSeason(SeasonType seasonType) {
        return type == AchievementType.SEASONAL && season == seasonType;
    }
}
