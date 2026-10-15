package dev.daweapon.type.skyblockgeneric.mission.missions.sheperd;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class MissionTalkToShepherd extends SkyBlockMission {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onVillagerSpokenTo(NPCInteractEvent event) {
        MissionData data = ((SkyBlockPlayer) event.getPlayer()).getMissionData();

        // Check if this is the Shepherd NPC
        if (!event.getNpc().getClass().getSimpleName().contains("Shepherd")) {
            return;
        }

        if (data.isCurrentlyActive(this.getClass()) || data.hasCompleted(this.getClass())) {
            return;
        }

        data.setSkyBlockPlayer((SkyBlockPlayer) event.getPlayer());
        data.startMission(this.getClass());
    }

    @Override
    public String getID() {
        return "talk_to_shepherd";
    }

    @Override
    public String getName() {
        return "Talk to the Shepherd";
    }

    @Override
    public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
        return new HashMap<>();
    }

    @Override
    public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
        // Start the next mission in the chain
        player.getMissionData().startMission(MissionShearSheep.class);
    }

    @Override
    public Set<RegionType> getValidRegions() {
        return Set.of(RegionType.SHEPHERD_KEEP);
    }
}
