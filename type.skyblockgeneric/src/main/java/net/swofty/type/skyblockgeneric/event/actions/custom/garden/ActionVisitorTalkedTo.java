package net.swofty.type.skyblockgeneric.event.actions.custom.garden;

import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.garden.visitor.VisitorService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionVisitorTalkedTo implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(NPCInteractEvent event) {
        if (!(event.player() instanceof SkyBlockPlayer player) || event.getNpc() == null) return;
        VisitorService.talkedTo(player, event.getNpc().getName());
    }
}
