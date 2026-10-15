package dev.daweapon.type.skyblockgeneric.mission.missions.marco;

import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MissionPaintTheCanvas extends SkyBlockMission {
    @Override
    public String getID() {
        return "paint_the_canvas";
    }

    @Override
    public String getName() {
        return "Paint the Canvas";
    }

    @Override
    public HashMap<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>()).forEach(player::sendMessage);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.CANVAS_ROOM);
    }
}
