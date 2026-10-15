package dev.daweapon.type.crystalhollows.events;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crystalhollows.DugTreasureChests;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HardStone;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;

public class ActionTreasureChestDig implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        if (region == null || !HardStone.isHardStone(event.getMaterial(), region.getType())) return;
        DugTreasureChests.onHardStoneMined(event.getPlayer(), event.getPoint());
    }
}
