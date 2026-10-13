package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionTalkToSamAtComposter extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_talk_to_sam_at_composter";
    }

    @Override
    public String getName() {
        return "Talk to Sam at the Composter";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionPurchaseFuel.class;
    }
}
