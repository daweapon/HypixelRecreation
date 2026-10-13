package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
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
