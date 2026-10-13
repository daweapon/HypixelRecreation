package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionBringWheatToSam extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_bring_wheat_to_sam";
    }

    @Override
    public String getName() {
        return "Bring 25 Wheat to Sam";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionTalkToSamAtComposter.class;
    }

    @Override
    public Double getAttachedSkyBlockXP() {
        return 5D;
    }
}
