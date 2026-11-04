package dev.daweapon.type.theend.race;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class EndRaceProgress {
    private static final String INTRODUCED = "the_end:end_race:introduced";
    private static final String CLAIMED = "the_end:end_race:claimed";

    private EndRaceProgress() {
    }

    public static boolean introduced(SkyBlockPlayer player) {
        return DatapointCrimsonIsle.of(player).getNumber(INTRODUCED) > 0;
    }

    public static void introduce(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(INTRODUCED, 1L));
    }

    public static int claimedTiers(SkyBlockPlayer player) {
        return (int) DatapointCrimsonIsle.of(player).getNumber(CLAIMED);
    }

    public static void setClaimedTiers(SkyBlockPlayer player, int tiers) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(CLAIMED, (long) tiers));
    }

    public static EndRaceTier nextTier(SkyBlockPlayer player) {
        int claimed = claimedTiers(player);
        return claimed >= EndRaceTier.values().length ? null : EndRaceTier.values()[claimed];
    }
}
