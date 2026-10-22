package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.type.skyblockgeneric.farmhand.FarmhandQuest;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionFarmhandSummonPet extends SkyBlockMission {
    @Override
    public String getID() {
        return "farmhand_summon_pet";
    }

    @Override
    public String getName() {
        return "Summon your Chicken Pet";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        return Map.of();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        FarmhandQuest.startCollections(player);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.THE_BARN);
    }
}
