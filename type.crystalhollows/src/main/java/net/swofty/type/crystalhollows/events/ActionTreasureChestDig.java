package net.swofty.type.crystalhollows.events;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.DugTreasureChests;
import net.swofty.type.skyblockgeneric.crystalhollows.HardStone;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

public class ActionTreasureChestDig implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        if (region == null || !HardStone.isHardStone(event.getMaterial(), region.getType())) return;
        DugTreasureChests.onHardStoneMined(event.getPlayer(), event.getPoint());
    }
}
