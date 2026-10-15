package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionTalkToSam extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_talk_to_sam";
    }

    @Override
    public String getName() {
        return "Talk to Sam";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionBringWheatToSam.class;
    }
}
