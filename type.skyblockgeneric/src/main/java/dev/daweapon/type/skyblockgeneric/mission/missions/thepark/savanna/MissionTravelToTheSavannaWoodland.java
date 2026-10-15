package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna;


import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionTravelToTheSavannaWoodland extends SkyBlockMission implements LocationAssociatedMission {


	@Override
	public Pos getLocation() {
		return new Pos(-401, 97, -34);
	}

	@Override
	public String getID() {
		return "travel_to_the_savanna_woodland";
	}

	@Override
	public String getName() {
		return "Visit the Savanna Woodland";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionCheckOnMelody.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.DARK_THICKET);
	}

}
