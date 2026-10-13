package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionTakeCompost extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_take_compost";
    }

    @Override
    public String getName() {
        return "Take Compost from the Composter";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionPurchasePlot.class;
    }

    @Override
    public Double getAttachedSkyBlockXP() {
        return 5D;
    }
}
