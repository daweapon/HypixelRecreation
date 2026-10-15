package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.jungle;


import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class MissionLeaveTheArea extends SkyBlockMission {

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void run(PlayerRegionChangeEvent event) {
		if (event.getTo() == null) return;
		MissionData data = event.getPlayer().getMissionData();
		if (event.getTo() == RegionType.JUNGLE_ISLAND) return;
		if (data.isCurrentlyActive(this.getClass())) {
			data.endMission(this.getClass());
		}
	}

	@Override
	public String getID() {
		return "leave_the_area";
	}

	@Override
	public String getName() {
		return "Leave the area";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionTalkToMolbertAgain.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.JUNGLE_ISLAND);
	}
}
