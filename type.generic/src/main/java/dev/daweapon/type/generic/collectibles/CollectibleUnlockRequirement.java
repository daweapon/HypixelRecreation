package dev.daweapon.type.generic.collectibles;

import dev.daweapon.type.generic.user.categories.Rank;

// Describes what is needed to unlock a collectible: method, required rank/currency/cost, or a custom resolver.
public record CollectibleUnlockRequirement(
    CollectibleUnlockMethod method,
    Rank requiredRank,
    CollectibleCurrency currency,
    Long cost,
    String customResolverKey,
    String customDisplayText,
    CollectibleEvent event
) {
    public CollectibleUnlockRequirement {
        if (method == null) {
            method = CollectibleUnlockMethod.FREE;
        }
        if (currency == null) {
            currency = CollectibleCurrency.BEDWARS_TOKENS;
        }
    }

    public static CollectibleUnlockRequirement free() {
        return new CollectibleUnlockRequirement(
            CollectibleUnlockMethod.FREE,
            null,
            CollectibleCurrency.BEDWARS_TOKENS,
            null,
            null,
            null,
            null
        );
    }

}
