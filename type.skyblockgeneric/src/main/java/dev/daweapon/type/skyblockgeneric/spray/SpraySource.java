package dev.daweapon.type.skyblockgeneric.spray;

import org.jetbrains.annotations.Nullable;

/** Where a spray comes from on Hypixel; only the default sprays can be unlocked on this server. */
public enum SpraySource {
    DEFAULT(null),
    REWARD_BUNDLE("Claim it from the Salesman's Reward Bundles."),
    RANK_GIFTING("Rank gifting isn't in this server."),
    ADVENT_CALENDAR("Claim it from the Advent Calendar.");

    private final @Nullable String unavailableReason;

    SpraySource(@Nullable String unavailableReason) {
        this.unavailableReason = unavailableReason;
    }

    public @Nullable String unavailableReason() {
        return unavailableReason;
    }

    public boolean isUnlocked() {
        return this == DEFAULT;
    }
}
