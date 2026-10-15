package dev.daweapon.type.skyblockgeneric.event.actions.custom.garden;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionVisitorTalkedTo implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(NPCInteractEvent event) {
        if (!(event.player() instanceof SkyBlockPlayer player) || event.getNpc() == null) return;
        VisitorService.talkedTo(player, event.getNpc().getName());
    }
}
