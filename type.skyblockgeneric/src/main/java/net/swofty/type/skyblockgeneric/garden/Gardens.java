package net.swofty.type.skyblockgeneric.garden;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointGarden;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorSlots;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
