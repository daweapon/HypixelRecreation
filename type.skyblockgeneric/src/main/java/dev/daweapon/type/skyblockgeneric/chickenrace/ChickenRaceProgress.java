package dev.daweapon.type.skyblockgeneric.chickenrace;

import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ChickenRaceProgress {
    private ChickenRaceProgress() {
    }

    public static int claimed(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static boolean hasClaimed(SkyBlockPlayer player, Cluckpoint cluckpoint) {
        return claimed(player) >= cluckpoint.number();
    }

    public static void setClaimed(SkyBlockPlayer player, int claimed) {
        datapoint(player).setValue(Math.clamp(claimed, 0, Cluckpoint.values().length));
    }

    public static boolean hasSpokenToFrosty(SkyBlockPlayer player) {
        return player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROSTY);
    }

    /** Marks every cluckpoint reached without granting the rewards, which stay one-time items. */
    public static void maxOut(SkyBlockPlayer player) {
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FROSTY, true);
        setClaimed(player, Cluckpoint.values().length);
    }

    private static DatapointInteger datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHICKEN_RACE_CLUCKPOINT, DatapointInteger.class);
    }
}
