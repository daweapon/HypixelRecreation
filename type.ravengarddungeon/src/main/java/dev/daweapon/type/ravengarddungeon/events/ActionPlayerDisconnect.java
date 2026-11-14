package dev.daweapon.type.ravengarddungeon.events;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.ravengarddungeon.TypeRavengardDungeonLoader;
import dev.daweapon.type.ravengarddungeon.user.RavengardDungeonPlayer;

// Notifies the dungeon game that a player has disconnected.
public class ActionPlayerDisconnect implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void run(PlayerDisconnectEvent event) {
        TypeRavengardDungeonLoader.getGame().disconnect((RavengardDungeonPlayer) event.getPlayer());
    }
}
