package net.swofty.type.skyblockgeneric.mission.missions.garden;

import net.swofty.type.skyblockgeneric.garden.quest.GardenQuestMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import org.jetbrains.annotations.Nullable;

public class MissionPurchaseFuel extends GardenQuestMission {
    @Override
    public String getID() {
        return "garden_purchase_fuel";
    }

    @Override
    public String getName() {
        return "Purchase Fuel from SkyMart";
    }

    @Override
    protected @Nullable Class<? extends SkyBlockMission> next() {
        return MissionRefuelComposter.class;
    }
}
