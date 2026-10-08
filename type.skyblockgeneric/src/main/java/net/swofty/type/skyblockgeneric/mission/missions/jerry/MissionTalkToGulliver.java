package net.swofty.type.skyblockgeneric.mission.missions.jerry;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.mission.LocationAssociatedMission;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionTalkToGulliver extends SkyBlockMission implements LocationAssociatedMission {

    @Override
    public Pos getLocation() {
        return new Pos(68.5, 105, 33.5);
    }

    @Override
    public String getID() {
        return "chicken_racing_talk_to_gulliver";
    }

    @Override
    public String getName() {
        return "Talk to Gulliver";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        return Map.of();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<b>5 SkyBlock XP"))).forEach(player::sendMessage);
        player.getMissionData().startMission(MissionReachCluckpointFour.class);
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
