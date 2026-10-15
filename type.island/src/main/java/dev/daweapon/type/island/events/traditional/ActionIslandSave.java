package dev.daweapon.type.island.events.traditional;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Checks a departing player's island for vacancy so it can be saved/cleaned up on disconnect.
public class ActionIslandSave implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.PERSIST, order = 20)
    public void run(PlayerDisconnectEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = player.getSkyBlockIsland();
        if (island != null) {
            island.runVacantCheck();
        }
    }
}
