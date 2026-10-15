package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.spruce.race;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionTalkToGustaveFourth extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public Pos getLocation() {
		return new Pos(-363.5, 89, 44.5);
	}

	@Override
	public String getID() {
		return "talk_to_gustave_third";
	}

	@Override
	public String getName() {
		return "Talk to Gustave";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionCompleteTheRaceThird.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.SPRUCE_WOODS);
	}
}
