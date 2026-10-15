package dev.daweapon.type.lobby.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

/**
 * Shared async configuration handler for all lobby types.
 */
public class LobbyPlayerJoinEvents implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(AsyncPlayerConfigurationEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        event.setSpawningInstance(HypixelConst.getInstanceContainer());
        Logger.info("Player " + player.getUsername() + " joined the server from start server " + player.getOriginServer());
        player.setRespawnPoint(HypixelConst.getTypeLoader()
                .getLoaderValues()
                .spawnPosition()
                .apply(player.getOriginServer())
        );
    }
}
