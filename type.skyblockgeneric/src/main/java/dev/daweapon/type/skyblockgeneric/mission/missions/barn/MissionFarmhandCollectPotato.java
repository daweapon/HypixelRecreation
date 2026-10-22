package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectPotato extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_potato";
    }

    @Override
    public String getName() {
        return "Collect Potato";
    }

    @Override
    public ItemType collected() {
        return ItemType.POTATO;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionFarmhandCollectCarrot.class;
    }
}
