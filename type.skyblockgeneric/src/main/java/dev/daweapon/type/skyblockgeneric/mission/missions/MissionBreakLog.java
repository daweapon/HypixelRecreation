package dev.daweapon.type.skyblockgeneric.mission.missions;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class MissionBreakLog extends SkyBlockMission {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void endMission(CustomBlockBreakEvent event) {
        if (event.getPlayerPlaced()) return;

        MissionData data = event.getPlayer().getMissionData();

        if (!data.isCurrentlyActive(MissionBreakLog.class) || data.hasCompleted(MissionBreakLog.class)) return;

        data.setSkyBlockPlayer(event.getPlayer());
        data.endMission(MissionBreakLog.class);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void startMission(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        MissionData data = ((SkyBlockPlayer) event.getPlayer()).getMissionData();

        if (data.isCurrentlyActive(MissionBreakLog.class) || data.hasCompleted(MissionBreakLog.class)) return;

        data.setSkyBlockPlayer(((SkyBlockPlayer) event.getPlayer()));
        data.startMission(MissionBreakLog.class);
    }

    @Override
    public String getID() {
        return "break_log";
    }

    @Override
    public String getName() {
        return "Break a log";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        player.getMissionData().startMission(MissionCraftWorkbench.class);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Collections.singleton(RegionType.PRIVATE_ISLAND);
    }
}
