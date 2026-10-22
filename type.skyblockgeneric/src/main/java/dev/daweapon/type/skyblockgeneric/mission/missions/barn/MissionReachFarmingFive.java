package dev.daweapon.type.skyblockgeneric.mission.missions.barn;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MissionReachFarmingFive extends SkyBlockMission {
    public static final int REQUIRED_LEVEL = 5;
    private static final double FARMING_XP = 300D;

    public static boolean reached(SkyBlockPlayer player) {
        return player.getSkills().getCurrentLevel(SkillCategories.FARMING) >= REQUIRED_LEVEL;
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onSkillUpdate(SkillUpdateEvent event) {
        if (event.getSkillCategory() != SkillCategories.FARMING) return;
        SkyBlockPlayer player = event.getPlayer();
        if (!player.getMissionData().isCurrentlyActive(getClass()) || !reached(player)) return;
        player.getMissionData().setSkyBlockPlayer(player);
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
            if (player.getMissionData().isCurrentlyActive(getClass())) player.getMissionData().endMission(getClass());
        });
    }

    @Override
    public String getID() {
        return "farmhand_farming_five";
    }

    @Override
    public String getName() {
        return "Reach Farming V";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        dev.daweapon.type.skyblockgeneric.farmhand.FarmhandQuest.syncLater(player);
        return Map.of();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        mission.getObjectiveCompleteText(new ArrayList<>(List.of("<3>300 <7>Farming Experience"))).forEach(player::sendMessage);
        MinecraftServer.getSchedulerManager().scheduleNextTick(() ->
                player.getSkills().increase(player, SkillCategories.FARMING, FARMING_XP));
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.THE_BARN);
    }
}
