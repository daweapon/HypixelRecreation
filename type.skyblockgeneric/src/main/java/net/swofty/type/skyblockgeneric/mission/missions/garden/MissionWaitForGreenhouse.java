package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionWaitForGreenhouse extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_wait_for_greenhouse";
    }

    @Override
    public String getName() {
        return "Wait for the Greenhouse to complete building";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return null;
    }

    @Override
    public Double getAttachedSkyBlockXP() {
        return 5D;
    }
}
