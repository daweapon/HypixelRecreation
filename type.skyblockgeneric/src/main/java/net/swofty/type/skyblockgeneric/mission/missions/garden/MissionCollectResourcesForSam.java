package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionCollectResourcesForSam extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_collect_resources_for_sam";
    }

    @Override
    public String getName() {
        return "Collect resources for Sam";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionPasteGreenhouse.class;
    }
}
