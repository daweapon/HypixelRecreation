package dev.daweapon.type.skyblockgeneric.mission.missions.blacksmith;


import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MissionTalkToBlacksmithAgain extends SkyBlockMission implements LocationAssociatedMission {
    @Override
    public String getID() {
        return "talk_to_blacksmith_again";
    }


    @Override
    public String getName() {
        return "Talk to the Blacksmith";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        player.getMissionData().startMission(MissionReforgeAnItem.class);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.MINING_DISTRICT, RegionType.COAL_MINE);
    }

    @Override
    public Pos getLocation() {
        return new Pos(10.5, 63, -126.5);
    }
}
