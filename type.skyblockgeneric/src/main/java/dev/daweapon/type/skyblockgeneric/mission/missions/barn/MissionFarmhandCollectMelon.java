package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectMelon extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_melon";
    }

    @Override
    public String getName() {
        return "Collect Melon Slice";
    }

    @Override
    public ItemType collected() {
        return ItemType.MELON_SLICE;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionFarmhandCollectPumpkin.class;
    }
}
