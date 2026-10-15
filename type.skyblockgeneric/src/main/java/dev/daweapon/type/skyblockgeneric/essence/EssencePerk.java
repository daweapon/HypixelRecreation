package dev.daweapon.type.skyblockgeneric.essence;

import org.jetbrains.annotations.Nullable;

/**
 * A tiered perk bought with one essence type. Saved as {@code "<ESSENCE>:<PERK>"} in the
 * per-profile essence perk map.
 */
public interface EssencePerk {
    String name();

    EssenceType essence();

    String getDisplayName();

    double[] getValues();

    long[] getCosts();

    @Nullable String getUnavailableReason();

    default int getMaxTier() {
        return getCosts().length;
    }

    default double valueAt(int tier) {
        return tier <= 0 ? 0 : getValues()[Math.min(tier, getValues().length) - 1];
    }

    default long costOfTier(int tier) {
        return getCosts()[tier - 1];
    }

    default long totalCost() {
        long total = 0;
        for (long cost : getCosts()) total += cost;
        return total;
    }
}
