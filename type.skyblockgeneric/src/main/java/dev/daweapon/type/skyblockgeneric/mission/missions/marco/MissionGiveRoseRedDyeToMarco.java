package dev.daweapon.type.skyblockgeneric.mission.missions.marco;

import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionGiveRoseRedDyeToMarco extends SkyBlockMission {
    public static final int REQUIRED_DYE = 5;

    @Override
    public String getID() {
        return "give_rose_red_dye_to_marco";
    }

    @Override
    public String getName() {
        return "Give Rose Red Dye to Marco";
    }

    @Override
    public HashMap<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<f>Spray Can"))).forEach(player::sendMessage);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.FLOWER_HOUSE);
    }
}
