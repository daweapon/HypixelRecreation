package dev.daweapon.type.skyblockgeneric.event.actions.farming;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.farming.animal.FarmAnimalAxes;

// Advanced animal axes add their extra drops to kills of the matching animal
public class ActionFarmAnimalAxes implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        FarmAnimalAxes.onKill(event.getPlayer(), event.getKilledMob());
    }
}
