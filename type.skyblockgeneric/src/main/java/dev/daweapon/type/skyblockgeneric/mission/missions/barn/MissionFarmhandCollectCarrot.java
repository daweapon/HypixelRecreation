package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectCarrot extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_carrot";
    }

    @Override
    public String getName() {
        return "Collect Carrot";
    }

    @Override
    public ItemType collected() {
        return ItemType.CARROT;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionFarmhandCollectMelon.class;
    }
}
