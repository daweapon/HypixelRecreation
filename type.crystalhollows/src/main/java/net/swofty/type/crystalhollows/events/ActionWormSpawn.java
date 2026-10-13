package net.swofty.type.crystalhollows.events;

import net.swofty.type.crystalhollows.worm.WormSpawning;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.HardStone;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

public class ActionWormSpawn implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(CustomBlockBreakEvent event) {
		if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
		SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
		RegionType regionType = region == null ? null : region.getType();
		if (!HardStone.isHardStone(event.getMaterial(), regionType)) return;
		WormSpawning.onHardStoneBroken(event.getPlayer(), event.getInstance(), event.getPoint());
	}
}
