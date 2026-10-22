package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;

public class MissionFarmhandCollectLeather extends FarmhandCollectionMission {
    @Override
    public String getID() {
        return "farmhand_collect_leather";
    }

    @Override
    public String getName() {
        return "Collect Leather";
    }

    @Override
    public ItemType collected() {
        return ItemType.LEATHER;
    }

    @Override
    protected Class<? extends SkyBlockMission> next() {
        return MissionFarmhandCollectChicken.class;
    }
}
