package net.swofty.type.skyblockgeneric.essence;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Frozen Alex's Ice Essence shop perks, with the wiki's per-tier values and costs. A perk with an
 * {@code unavailableReason} can't be bought until its effect is wired.
 */
@Getter
public enum IceEssencePerk implements EssencePerk {
    COLD_EFFICIENCY("Cold Efficiency", new double[]{2, 4, 6, 8, 10},
            new long[]{1_000, 1_500, 2_000, 3_000, 5_000}, "Dungeon classes aren't in this server yet."),
    COOLED_FORGES("Cooled Forges", new double[]{4, 8, 12, 16, 20},
            new long[]{100, 1_000, 2_000, 3_000, 5_000}, null),
    FROZEN_SKIN("Frozen Skin", new double[]{1, 2, 3, 4, 5},
            new long[]{1_000, 1_500, 2_000, 3_000, 5_000}, null),
    SEASON_OF_JOY("Season of Joy", new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
            new long[]{200, 400, 800, 1_500, 2_100, 3_000, 3_000, 4_000, 4_000, 4_000}, null),
    DRAKE_PIPER("Drake Piper", new double[]{10},
            new long[]{2_000}, null);

    private final String displayName;
    private final double[] values;
    private final long[] costs;
    private final @Nullable String unavailableReason;

    IceEssencePerk(String displayName, double[] values, long[] costs, @Nullable String unavailableReason) {
        this.displayName = displayName;
        this.values = values;
        this.costs = costs;
        this.unavailableReason = unavailableReason;
    }

    @Override
    public EssenceType essence() {
        return EssenceType.ICE;
    }
}
