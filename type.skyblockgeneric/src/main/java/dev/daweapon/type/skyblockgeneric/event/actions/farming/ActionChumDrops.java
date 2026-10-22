package dev.daweapon.type.skyblockgeneric.event.actions.farming;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.farming.chum.ChumDrops;

// Sea creatures killed near placed Chum Buckets, or with a Stiff rod, drop Chum
public class ActionChumDrops implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        ChumDrops.onKill(event.getPlayer(), event.getKilledMob());
    }
}
