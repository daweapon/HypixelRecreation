package dev.daweapon.type.skyblockgeneric.fishing.ship;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointShipState;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipPartAwardsTest {
    @Test
    void onlyBronzePartsAward() {
        assertEquals(0, ShipPartAwards.skyBlockXp(new DatapointShipState.ShipState()));
        assertEquals(5, ShipPartAwards.skyBlockXp(new DatapointShipState.ShipState("Z", "BRONZE_SHIP_HELM",
                "RUSTY_SHIP_ENGINE", "RUSTY_SHIP_HULL", new ArrayList<>())));
    }

    @Test
    void allBronzePartsReachTheSourceCap() {
        DatapointShipState.ShipState state = new DatapointShipState.ShipState("Z", "BRONZE_SHIP_HELM",
                "BRONZE_SHIP_ENGINE", "BRONZE_SHIP_HULL", new ArrayList<>());
        assertEquals(SkyBlockXPSource.SHIP_PARTS.getMaxXP(), ShipPartAwards.skyBlockXp(state));
    }
}
