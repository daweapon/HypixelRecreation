package net.swofty.type.crimsonisle.events;

import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crimson.mob.MatchoEruptions;
import net.swofty.type.skyblockgeneric.crimson.volcano.VolcanoEruptionEvent;

// The Coal Blocks each Blazing Volcano eruption flings come to life as Matchos.
public class ActionVolcanoMatcho implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(VolcanoEruptionEvent event) {
        if (!event.started()) return;
        MatchoEruptions.erupt(HypixelConst.getInstanceContainer(), event.explosivity());
    }
}
