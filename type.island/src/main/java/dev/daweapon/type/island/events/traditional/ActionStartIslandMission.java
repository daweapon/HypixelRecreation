package dev.daweapon.type.island.events.traditional;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.missions.MissionBreakLog;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Starts the "break log" tutorial mission for new players on their Island.
public class ActionStartIslandMission implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_DATA)
    public void run(AsyncPlayerConfigurationEvent event) {
        MissionData data = ((SkyBlockPlayer) event.getPlayer()).getMissionData();

        if (data.isCurrentlyActive("break_log")) return;
        if (data.hasCompleted("break_log")) return;

        data.startMission(MissionBreakLog.class);
    }
}
