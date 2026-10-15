package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionBringBreadWheat extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_bring_bread_wheat";
    }

    @Override
    public String getName() {
        return "Bring 100 Wheat to Sam";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionSetUpStand.class;
    }
}
