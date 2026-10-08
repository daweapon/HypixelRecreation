package net.swofty.type.crimsonisle.events;

import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.SulphurOres;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoSpewEvent;

// Each Blazing Volcano eruption scatters Sulphur Ore over the island, a share with every spew wave.
public class ActionVolcanoSulphurOre implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(VolcanoSpewEvent event) {
        SulphurOres.scatter(HypixelConst.getInstanceContainer(), event.share(SulphurOres.countFor(event.explosivity())));
    }
}
