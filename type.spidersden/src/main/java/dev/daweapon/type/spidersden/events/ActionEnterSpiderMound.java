package dev.daweapon.type.spidersden.events;

import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

public class ActionEnterSpiderMound implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        if (event.getTo() != RegionType.SPIDER_MOUND) return;
        event.getPlayer().getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_ENTERED_SPIDER_MOUND, true);
    }
}
