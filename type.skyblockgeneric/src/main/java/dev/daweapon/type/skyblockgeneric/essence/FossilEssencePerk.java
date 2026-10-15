package dev.daweapon.type.skyblockgeneric.essence;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/** Researcher Beryl's Fossil Essence shop perks, with the wiki's per-tier values and costs. */
@Getter
public enum FossilEssencePerk implements EssencePerk {
    SLEIGHT_OF_HAND("Sleight Of Hand", new double[]{1}, new long[]{2_000}, null),
    CHILLED_TO_THE_BONE("Chilled To The Bone", valueSteps(2, 2, 10), costSteps(200, 200, 10),
            "Skeleton Fish can't be fished up on this server yet."),
    DWARVEN_EXPERTISE("Dwarven Expertise", valueSteps(2, 2, 10), costSteps(100, 100, 10), null),
    CUT_LOOSE("Cut Loose", new double[]{0.5, 1, 1.5, 2, 2.5}, costSteps(500, 500, 5),
            "Glacite Mineshafts aren't in this server yet."),
    PREHISTORIAN("Prehistorian", valueSteps(1, 1, 10), costSteps(50, 50, 10), "Dyes aren't in this server yet."),
    RESOURCEFUL("Resourceful", valueSteps(1, 1, 5), new long[]{2_500, 5_000, 7_500, 10_000, 12_500},
            "Glacite Mineshafts aren't in this server yet.");

    private final String displayName;
    private final double[] values;
    private final long[] costs;
    private final @Nullable String unavailableReason;

    FossilEssencePerk(String displayName, double[] values, long[] costs, @Nullable String unavailableReason) {
        this.displayName = displayName;
        this.values = values;
        this.costs = costs;
        this.unavailableReason = unavailableReason;
    }

    @Override
    public EssenceType essence() {
        return EssenceType.FOSSIL;
    }

    private static double[] valueSteps(double first, double step, int count) {
        double[] values = new double[count];
        for (int i = 0; i < count; i++) values[i] = first + step * i;
        return values;
    }

    private static long[] costSteps(long first, long step, int count) {
        long[] values = new long[count];
        for (int i = 0; i < count; i++) values[i] = first + step * i;
        return values;
    }
}
