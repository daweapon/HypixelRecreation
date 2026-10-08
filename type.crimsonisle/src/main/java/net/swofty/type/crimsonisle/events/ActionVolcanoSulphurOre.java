package net.swofty.type.crimsonisle.events;

import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.SulphurOres;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionEvent;

// Each Blazing Volcano eruption scatters Sulphur Ore over the island.
public class ActionVolcanoSulphurOre implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(VolcanoEruptionEvent event) {
        if (!event.started()) return;
        SulphurOres.scatter(HypixelConst.getInstanceContainer(), event.explosivity());
    }
}
