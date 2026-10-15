package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.birchpark;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import dev.daweapon.type.skyblockgeneric.mission.LocationAssociatedMission;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;

public class MissionGiveCharlieBirchLogs extends SkyBlockMission implements LocationAssociatedMission {

	@Override
	public String getID() {
		return "give_charlie_birch_logs";
	}

	@Override
	public String getName() {
		return "Give Charlie Birch Logs";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionClaimTheTrousers.class);
		player.openView(new ClaimRewardView(), new ClaimRewardView.State(ItemType.CHARLIE_TROUSERS, () -> {
			player.getMissionData().endMission(MissionClaimTheTrousers.class);
		}));
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.BIRCH_PARK);
	}

	@Override
	public Pos getLocation() {
		return new Pos(-277.5, 80, -17.5);
	}
}
