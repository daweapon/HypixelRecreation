package net.swofty.type.hub.events;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.hub.gui.GUIRunicPedestal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the Runic Pedestal GUI when a player interacts with the rune pedestal block in the Hub.
public class ActionPlayerClicksOnRunePedestal implements HypixelEventClass {


    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (!event.getBlockPosition().equals(new BlockVec(23, 64, -135))) return;

        new GUIRunicPedestal().open((SkyBlockPlayer) event.getPlayer());
    }
}
