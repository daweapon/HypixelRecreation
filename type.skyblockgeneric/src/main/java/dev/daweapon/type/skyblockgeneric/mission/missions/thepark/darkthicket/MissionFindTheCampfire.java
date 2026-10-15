package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.darkthicket;


import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionFindTheCampfire extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public Pos getLocation() {
		return new Pos(-363, 102, -94);
	}

	@Override
	public String getID() {
		return "find_the_campfire";
	}

	@Override
	public String getName() {
		return "Find the Campfire";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionSneakUpOnRyan.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.DARK_THICKET, RegionType.TRIALS_OF_FIRE);
	}
}
