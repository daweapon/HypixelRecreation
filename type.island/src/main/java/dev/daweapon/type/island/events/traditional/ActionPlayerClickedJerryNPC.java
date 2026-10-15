package dev.daweapon.type.island.events.traditional;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.island.gui.GUIJerry;
import dev.daweapon.type.skyblockgeneric.event.custom.JerryClickedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the Jerry GUI when a player right-clicks the Jerry villager NPC on their island.
public class ActionPlayerClickedJerryNPC implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (event.getHand() != PlayerHand.MAIN) return;
        if (event.getTarget().getEntityType() != EntityType.VILLAGER) return;

        JerryClickedEvent jerryClickedEvent = new JerryClickedEvent(player);
        HypixelEventHandler.callCustomEvent(jerryClickedEvent);

        if (jerryClickedEvent.isCancelled()) return;

        new GUIJerry().open(player);
    }
}
