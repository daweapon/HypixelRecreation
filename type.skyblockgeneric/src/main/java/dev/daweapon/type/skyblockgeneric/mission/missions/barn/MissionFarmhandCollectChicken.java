package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectChicken extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_chicken";
    }

    @Override
    public String getName() {
        return "Collect Raw Chicken";
    }

    @Override
    public ItemType collected() {
        return ItemType.RAW_CHICKEN;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionFarmhandCollectPork.class;
    }
}
