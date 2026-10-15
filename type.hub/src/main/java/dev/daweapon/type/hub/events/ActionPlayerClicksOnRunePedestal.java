package dev.daweapon.type.hub.events;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.hub.gui.GUIRunicPedestal;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the Runic Pedestal GUI when a player interacts with the rune pedestal block in the Hub.
public class ActionPlayerClicksOnRunePedestal implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (!event.getBlockPosition().equals(new BlockVec(23, 64, -135))) return;

        new GUIRunicPedestal().open((SkyBlockPlayer) event.getPlayer());
    }
}
