package dev.daweapon.type.skyblockgeneric.bestiary;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class BestiaryMilestones {
    private BestiaryMilestones() {
    }

    public static int of(SkyBlockPlayer player) {
        return BestiaryExperience.totalTiers(player.getBestiaryData()) / BestiaryExperience.TIERS_PER_MILESTONE;
    }
}
