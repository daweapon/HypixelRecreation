package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectPork extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_pork";
    }

    @Override
    public String getName() {
        return "Collect Raw Porkchop";
    }

    @Override
    public ItemType collected() {
        return ItemType.RAW_PORKCHOP;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionReachFarmingFive.class;
    }
}
