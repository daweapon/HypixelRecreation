package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionGiveSamBlueprint extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_give_sam_blueprint";
    }

    @Override
    public String getName() {
        return "Give Sam the Greenhouse Blueprint";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionCollectResourcesForSam.class;
    }
}
