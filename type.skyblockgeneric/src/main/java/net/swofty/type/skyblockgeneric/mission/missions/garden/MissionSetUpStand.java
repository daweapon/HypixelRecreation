package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionSetUpStand extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_set_up_stand";
    }

    @Override
    public String getName() {
        return "Set up the Visitor Stand";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionServeFirstCustomer.class;
    }
}
