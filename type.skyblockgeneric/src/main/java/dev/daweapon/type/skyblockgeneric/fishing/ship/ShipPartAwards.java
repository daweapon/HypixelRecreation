package dev.daweapon.type.skyblockgeneric.fishing.ship;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointShipState;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Ship Parts (wiki: Ship): +5 SkyBlock XP for each of the Bronze Ship Helm, Engine and Hull applied to the ship.
 */
public final class ShipPartAwards {
    public static final int XP_PER_PART = 5;

    private ShipPartAwards() {
    }

    public static int skyBlockXp(DatapointShipState.ShipState state) {
        int parts = 0;
        if ("BRONZE_SHIP_HELM".equals(state.getHelm())) parts++;
        if ("BRONZE_SHIP_ENGINE".equals(state.getEngine())) parts++;
        if ("BRONZE_SHIP_HULL".equals(state.getHull())) parts++;
        return parts * XP_PER_PART;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.SHIP_PARTS, skyBlockXp(player.getShipState()));
    }
}
