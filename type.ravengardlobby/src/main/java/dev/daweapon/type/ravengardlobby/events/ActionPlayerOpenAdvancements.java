package dev.daweapon.type.ravengardlobby.events;

import net.minestom.server.advancements.AdvancementAction;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengardgeneric.gui.GUIRavengardMenu;
import dev.daweapon.type.ravengardgeneric.user.RavengardPlayer;

// Detects the client opening the advancement tab (via the detector tab packet) and opens the Ravengard menu GUI instead.
public class ActionPlayerOpenAdvancements implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, phase = EventPhase.GAMEPLAY)
    public void run(PlayerPacketEvent event) {
        if (event.getPacket() instanceof ClientAdvancementTabPacket packet) {
            if (packet.action() == AdvancementAction.OPENED_TAB) {
                if (event.getPlayer() instanceof RavengardPlayer player) {
                    player.openView(new GUIRavengardMenu());
                }
            }
        }
    }
}
