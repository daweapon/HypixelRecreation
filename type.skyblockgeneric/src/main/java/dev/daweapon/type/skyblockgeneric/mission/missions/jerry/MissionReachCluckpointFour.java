package dev.daweapon.type.skyblockgeneric.mission.missions.jerry;

import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionReachCluckpointFour extends SkyBlockMission {

    @Override
    public String getID() {
        return "chicken_racing_reach_checkpoint_4";
    }

    @Override
    public String getName() {
        return "Reach checkpoint 4";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return Map.of();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<b>5 SkyBlock XP"))).forEach(player::sendMessage);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.JERRYS_WORKSHOP, RegionType.JERRY_POND, RegionType.TERRYS_SHACK, RegionType.MOUNT_JERRY,
                RegionType.HOT_SPRINGS, RegionType.GARYS_SHACK);
    }

    @Override
    public Double getAttachedSkyBlockXP() {
        return 5D;
    }
}
