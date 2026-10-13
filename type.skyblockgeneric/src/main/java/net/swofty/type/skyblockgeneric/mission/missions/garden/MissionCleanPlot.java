package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionCleanPlot extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_clean_plot";
    }

    @Override
    public String getName() {
        return "Clean your Garden Plot";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionTalkToSamPlotCleaned.class;
    }
}
