package dev.daweapon.type.ravengardlobby.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengardlobby.TypeRavengardLobbyLoader;

// On a player's first spawn, adds them as a viewer of the advancement detector tab so their client reports tab-open packets.
public class ActionPlayerSpawn implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        if (event.isFirstSpawn()) {
            // adds the "detector tab" to the player. this way the ClientAdvancementTabPacket with OPENED_TAB is sent from the client
            TypeRavengardLobbyLoader.detectorTab.addViewer(event.getPlayer());
        }
    }
}
