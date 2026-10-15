package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionTalkToSamPlotCleaned extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_talk_to_sam_plot_cleaned";
    }

    @Override
    public String getName() {
        return "Talk to Sam";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionBringBreadWheat.class;
    }

    @Override
    public Double getAttachedSkyBlockXP() {
        return 5D;
    }
}
