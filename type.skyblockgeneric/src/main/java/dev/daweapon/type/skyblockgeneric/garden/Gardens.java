package dev.daweapon.type.skyblockgeneric.garden;

import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointGarden;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorSlots;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class Gardens {
    private Gardens() {
    }

    public static GardenProfile profile(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, GardenProfile profile) {
        datapoint(player).setValue(profile);
        VisitorSlots.refresh(player.getUuid(), profile.visitors());
    }

    private static DatapointGarden datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GARDEN, DatapointGarden.class);
    }
}
