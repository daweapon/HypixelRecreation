package dev.daweapon.type.skyblockgeneric.event.actions.custom.rare;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crystalhollows.GoblinEggs;
import dev.daweapon.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;

public class ActionGoldenGoblinOreSpawn implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        RegionType type = region == null ? null : region.getType();
        GoblinEggs.spawnFromOre(event.getPlayer(), event.getMaterial(), type, event.getPoint());
    }
}
