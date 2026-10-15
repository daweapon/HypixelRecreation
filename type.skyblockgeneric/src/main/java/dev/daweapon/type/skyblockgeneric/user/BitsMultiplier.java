package dev.daweapon.type.skyblockgeneric.user;

import dev.daweapon.type.skyblockgeneric.bank.PersonalBankService;
import dev.daweapon.type.skyblockgeneric.elections.FameRank;
import dev.daweapon.type.skyblockgeneric.museum.MuseumMilestones;

public final class BitsMultiplier {
    private BitsMultiplier() {}

    public static double of(SkyBlockPlayer player) {
        return FameRank.forFame(player.getFame()).getBitsMultiplier()
                + MuseumMilestones.bitsMultiplierBonus(PersonalBankService.data(player).getMuseumMilestone());
    }
}
