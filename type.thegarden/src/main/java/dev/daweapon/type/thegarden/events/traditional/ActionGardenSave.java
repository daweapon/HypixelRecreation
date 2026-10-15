package dev.daweapon.type.thegarden.events.traditional;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.garden.SkyBlockGarden;

public class ActionGardenSave implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.PERSIST, order = 20)
    public void run(PlayerDisconnectEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockGarden.getPresence().departed(player);
        if (player.getSkyBlockIsland() != null) {
            SkyBlockGarden.of(player).runVacantCheck();
        }
    }
}
