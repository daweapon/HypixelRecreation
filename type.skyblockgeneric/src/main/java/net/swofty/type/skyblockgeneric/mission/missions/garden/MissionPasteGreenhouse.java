package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
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
