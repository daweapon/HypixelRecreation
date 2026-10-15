package dev.daweapon.type.skyblockgeneric.garden.quest;

import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GardenQuestMission extends SkyBlockMission {
    protected abstract @Nullable Class<? extends SkyBlockMission> next();

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        mission.getNewObjectiveText().forEach(player::sendMessage);
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        if (getAttachedSkyBlockXP() > 0) {
            mission.getQuestCompleteText(new ArrayList<>(List.of("<b>" + getAttachedSkyBlockXP().intValue() + " SkyBlock XP")))
                    .forEach(player::sendMessage);
        }
        Class<? extends SkyBlockMission> next = next();
        if (next != null) {
            player.getMissionData().startMission(next);
        }
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.THE_GARDEN);
    }
}
