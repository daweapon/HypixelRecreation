package dev.daweapon.type.hub.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.museum.GUIYourMuseum;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the player's own museum GUI when they interact with the museum entrance block in the Hub.
public class ActionPlayerClicksMuseumNPCDisplay implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        Pos displayPosition = new Pos(-23, 67, 80);

        if (event.getBlockPosition().distance(displayPosition) > 2) return;

        player.openView(new GUIYourMuseum());
    }
}
