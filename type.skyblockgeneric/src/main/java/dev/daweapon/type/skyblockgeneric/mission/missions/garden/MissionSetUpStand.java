package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
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
