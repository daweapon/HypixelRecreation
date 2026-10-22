package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.farmhand.FarmhandQuest;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class FarmhandCollectionMission extends SkyBlockMission {
    private static final double FARMING_XP = 10D;

    public abstract ItemType collected();

    protected abstract Class<? extends SkyBlockMission> next();

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        FarmhandQuest.syncLater(player);
        return Map.of();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<3>10 <7>Farming Experience"))).forEach(player::sendMessage);
        player.getSkills().increase(player, SkillCategories.FARMING, FARMING_XP);
        Class<? extends SkyBlockMission> next = next();
        if (next == MissionReachFarmingFive.class) {
            FarmhandQuest.startFarmingFive(player);
        } else {
            player.getMissionData().startMission(next);
        }
        FarmhandQuest.syncLater(player);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.THE_BARN);
    }
}
