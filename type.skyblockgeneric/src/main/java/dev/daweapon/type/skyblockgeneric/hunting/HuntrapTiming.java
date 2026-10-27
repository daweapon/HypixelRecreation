package dev.daweapon.type.skyblockgeneric.hunting;

import java.time.Duration;

// Catch time of a Huntrap: a random time bounded by the rolled Shard's rarity, cut by the trap's
// tier first and then by the additive Desert Temple and Forest/Combat Trap discounts.
public final class HuntrapTiming {
    private HuntrapTiming() {
    }

    public static Duration minimum(AttributeRarity rarity) {
        return Duration.ofHours(switch (rarity) {
            case COMMON -> 8;
            case UNCOMMON -> 10;
            case RARE -> 12;
            case EPIC -> 14;
            case LEGENDARY -> 16;
        });
    }

    public static Duration maximum(AttributeRarity rarity) {
        return Duration.ofHours(switch (rarity) {
            case COMMON -> 12;
            case UNCOMMON -> 15;
            case RARE -> 18;
            case EPIC -> 21;
            case LEGENDARY -> 24;
        });
    }

    public static double multiplier(HuntrapTier tier, double additiveDiscount) {
        return (1 - tier.reduction()) * Math.max(0, 1 - additiveDiscount);
    }

    public static long millis(AttributeRarity rarity, HuntrapTier tier, double additiveDiscount, double random) {
        long low = minimum(rarity).toMillis();
        long high = maximum(rarity).toMillis();
        return Math.round((low + (high - low) * random) * multiplier(tier, additiveDiscount));
    }
}
