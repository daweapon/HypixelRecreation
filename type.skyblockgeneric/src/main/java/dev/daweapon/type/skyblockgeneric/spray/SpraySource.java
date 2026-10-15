package dev.daweapon.type.skyblockgeneric.spray;

import org.jetbrains.annotations.Nullable;

/** Where a spray comes from on Hypixel; only the default sprays can be unlocked on this server. */
public enum SpraySource {
    DEFAULT(null),
    REWARD_BUNDLE("Reward Bundles aren't in this server yet."),
    RANK_GIFTING("Rank gifting isn't in this server."),
    ADVENT_CALENDAR("The Advent Calendar isn't in this server yet.");

    private final @Nullable String unavailableReason;

    SpraySource(@Nullable String unavailableReason) {
        this.unavailableReason = unavailableReason;
    }

    public @Nullable String unavailableReason() {
        return unavailableReason;
    }

    public boolean isUnlocked() {
        return unavailableReason == null;
    }
}
