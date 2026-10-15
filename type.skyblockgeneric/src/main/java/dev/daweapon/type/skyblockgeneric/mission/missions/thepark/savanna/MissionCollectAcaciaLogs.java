package dev.daweapon.type.skyblockgeneric.mission.missions.thepark.savanna;


import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.mission.MissionData;
import dev.daweapon.type.skyblockgeneric.mission.SkyBlockProgressMission;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class MissionCollectAcaciaLogs extends SkyBlockProgressMission {

	private final Map<UUID, Long> testTimes = new HashMap<>();

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void onTick(PlayerTickEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		// requireDataLoaded=false means this fires before the SkyBlock data guard runs, and a
		// player entity can keep ticking briefly after a disconnect races their data unload -
		// skip instead of throwing when that happens, since it was crashing TPS server-wide.
		if (SkyBlockDataHandler.getUser(player) == null) return;
		if (testTimes.containsKey(player.getUuid())) {
			long lastTime = testTimes.get(player.getUuid());
			if (System.currentTimeMillis() - lastTime < 650) {
				return;
			}
		}

		MissionData data = player.getMissionData();

		if (!data.isCurrentlyActive(this.getClass()) || data.hasCompleted(this.getClass())) {
			testTimes.remove(player.getUuid());
			return;
		}

		testTimes.put(player.getUuid(), System.currentTimeMillis());

		int amount = 0;
		for (SkyBlockItem item : player.getAllInventoryItems()) {
			if (item.getMaterial() == Material.ACACIA_LOG || item.getMaterial() == Material.ACACIA_WOOD) {
				amount += item.getAmount();
			}
		}

		for (SkyBlockItem item : player.getAllSacks()) {
			if (item.getMaterial() == Material.ACACIA_LOG || item.getMaterial() == Material.ACACIA_WOOD) {
				amount += item.getAmount();
			}
		}

		MissionData.ActiveMission mission = data.getMission(this.getClass()).getKey();
		mission.setMissionProgress(amount);
		mission.checkIfMissionEnded(player);
	}

	@Override
	public int getMaxProgress() {
		return 512;
	}

	@Override
	public String getID() {
		return "collect_acacia_logs";
	}

	@Override
	public String getName() {
		return "Collect Acacia Logs";
	}

	@Override
	public Map<String, Object> onStart(SkyBlockPlayer player, MissionData.ActiveMission mission) {
		mission.getNewObjectiveText().forEach(player::sendMessage);
		return Map.of();
	}

	@Override
	public void onEnd(SkyBlockPlayer player, Map<String, Object> customData, MissionData.ActiveMission mission) {
		player.getMissionData().startMission(MissionGiveMelodyAcaciaLogs.class);
	}

	@Override
	public Set<RegionType> getValidRegions() {
		return Set.of(RegionType.SAVANNA_WOODLAND, RegionType.MELODY_PLATEAU);
	}
}
