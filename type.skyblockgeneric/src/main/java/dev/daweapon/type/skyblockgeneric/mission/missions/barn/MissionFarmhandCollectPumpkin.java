package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectPumpkin extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_pumpkin";
    }

    @Override
    public String getName() {
        return "Collect Pumpkin";
    }

    @Override
    public ItemType collected() {
        return ItemType.PUMPKIN;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionFarmhandCollectLeather.class;
    }
}
