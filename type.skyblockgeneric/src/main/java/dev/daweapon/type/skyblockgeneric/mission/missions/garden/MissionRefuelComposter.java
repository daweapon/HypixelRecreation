package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionRefuelComposter extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_refuel_composter";
    }

    @Override
    public String getName() {
        return "Put Fuel in the Composter";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionTakeCompost.class;
    }
}
