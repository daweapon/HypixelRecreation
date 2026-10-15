package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.jungle;


import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionGiveMolbertJungleLogs extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public Pos getLocation() {
		return new Pos(-447.5, 120, -63.5);
	}

	@Override
	public String getID() {
		return "give_molbert_jungle_logs";
	}

	@Override
	public String getName() {
		return "Give Molbert Jungle Logs";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionLeaveTheArea.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.JUNGLE_ISLAND);
	}
}
