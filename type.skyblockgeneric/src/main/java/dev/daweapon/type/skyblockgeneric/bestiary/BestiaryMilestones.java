package dev.daweapon.type.skyblockgeneric.bestiary;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** A Bestiary milestone is one family tier, so the player's milestone count is their total family tiers. */
public final class BestiaryMilestones {
    private BestiaryMilestones() {
    }

    public static int of(SkyBlockPlayer player) {
        return BestiaryExperience.totalTiers(player.getBestiaryData());
    }
}
