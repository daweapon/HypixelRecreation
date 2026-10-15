package dev.daweapon.type.crimsonisle.events;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MatchoEruptions;
import dev.daweapon.type.skyblockgeneric.crimson.volcano.VolcanoSpewEvent;

// The Coal Blocks each Blazing Volcano eruption flings come to life as Matchos.
public class ActionVolcanoMatcho implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(VolcanoSpewEvent event) {
        MatchoEruptions.erupt(HypixelConst.getInstanceContainer(),
                event.share(MatchoEruptions.countFor(event.explosivity())));
    }
}
