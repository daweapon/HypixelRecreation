package dev.daweapon.type.skyblockgeneric.mission.missions.garden;

import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuestMission;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionPasteGreenhouse extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_paste_greenhouse";
    }

    @Override
    public String getName() {
        return "Paste the Greenhouse on a plot at the Desk";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionWaitForGreenhouse.class;
    }
}
