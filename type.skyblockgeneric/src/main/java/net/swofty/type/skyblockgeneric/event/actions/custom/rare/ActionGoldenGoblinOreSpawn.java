package net.swofty.type.skyblockgeneric.event.actions.custom.rare;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.GoblinEggs;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

public class ActionGoldenGoblinOreSpawn implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(CustomBlockBreakEvent event) {
        if (Boolean.TRUE.equals(event.getPlayerPlaced())) return;
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getPoint());
        RegionType type = region == null ? null : region.getType();
        GoblinEggs.spawnFromOre(event.getPlayer(), event.getMaterial(), type, event.getPoint());
    }
}
