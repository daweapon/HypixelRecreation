package dev.daweapon.type.skyblockgeneric.fishing;

import dev.daweapon.commons.ServerType;

import java.util.random.RandomGenerator;

/**
 * Hypixel's bite timer (wiki: Fishing Speed): a base wait of 200–400 ticks, shortened by
 * {@code Fishing Speed / cap} of itself. The cap is 350 on the Crimson Isle, 200 in the Backwater Bayou and
 * 300 elsewhere; at or above the cap the fish bites at once.
 */
public final class FishingSpeed {
    public static final int MIN_BASE_TICKS = 200;
    public static final int MAX_BASE_TICKS = 400;

    private FishingSpeed() {
    }

    public static double cap(ServerType server) {
        if (server == null) return 300D;
        return switch (server) {
            case SKYBLOCK_CRIMSON_ISLE -> 350D;
            case SKYBLOCK_BACKWATER_BAYOU -> 200D;
            default -> 300D;
        };
    }

    public static long waitTicks(double fishingSpeed, double cap, RandomGenerator random) {
        int base = MIN_BASE_TICKS + random.nextInt(MAX_BASE_TICKS - MIN_BASE_TICKS + 1);
        double fraction = Math.min(1D, Math.max(0D, fishingSpeed) / cap);
        return Math.max(1L, Math.round(base - fraction * base));
    }
}
