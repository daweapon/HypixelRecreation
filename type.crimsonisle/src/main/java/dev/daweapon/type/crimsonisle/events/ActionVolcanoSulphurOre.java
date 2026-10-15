package dev.daweapon.type.crimsonisle.events;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.SulphurOres;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoSpewEvent;

// Each Blazing Volcano eruption scatters Sulphur Ore over the island, a share with every spew wave.
public class ActionVolcanoSulphurOre implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(VolcanoSpewEvent event) {
        SulphurOres.scatter(HypixelConst.getInstanceContainer(), event.share(SulphurOres.countFor(event.explosivity())));
    }
}
