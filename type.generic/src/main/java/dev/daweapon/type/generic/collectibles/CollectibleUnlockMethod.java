package dev.daweapon.type.generic.collectibles;

import java.util.Locale;

// The ways a collectible can be unlocked (free, rank, manual grant, currency purchase, custom, bedwars level).
public enum CollectibleUnlockMethod {
    FREE,
    RANK,
    MANUAL,
    CURRENCY,
    CUSTOM,
    BEDWARS_LEVEL;

    public static CollectibleUnlockMethod fromString(String value, CollectibleUnlockMethod fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return CollectibleUnlockMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
