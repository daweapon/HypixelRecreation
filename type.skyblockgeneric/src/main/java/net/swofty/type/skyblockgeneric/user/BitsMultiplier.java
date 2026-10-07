package net.swofty.type.skyblockgeneric.user;

import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.elections.FameRank;
import net.swofty.type.skyblockgeneric.museum.MuseumMilestones;

public final class BitsMultiplier {
    private BitsMultiplier() {}

    public static double of(SkyBlockPlayer player) {
        return FameRank.forFame(player.getFame()).getBitsMultiplier()
                + MuseumMilestones.bitsMultiplierBonus(PersonalBankService.data(player).getMuseumMilestone());
    }
}
