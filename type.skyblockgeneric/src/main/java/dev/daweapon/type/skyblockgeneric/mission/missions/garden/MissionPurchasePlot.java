package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
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
