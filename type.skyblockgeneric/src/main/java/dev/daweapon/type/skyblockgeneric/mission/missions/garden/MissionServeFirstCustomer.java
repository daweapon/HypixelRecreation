package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionServeFirstCustomer extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_serve_first_customer";
    }

    @Override
    public String getName() {
        return "Serve your first customer";
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
