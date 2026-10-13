package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionPurchasePlot extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_purchase_plot";
    }

    @Override
    public String getName() {
        return "Purchase a Garden Plot at the Desk";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionCleanPlot.class;
    }
}
