package net.swofty.type.hub.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

// Handles a player's initial configuration when joining the Hub, setting their spawn instance and respawn point.
public class ActionPlayerJoin implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void run(AsyncPlayerConfigurationEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        event.setSpawningInstance(HypixelConst.getInstanceContainer());
        Logger.info("Player " + player.getUsername() + " joined the server from start server " + player.getOriginServer());
        player.setRespawnPoint(HypixelConst.getTypeLoader()
                .getLoaderValues()
                .spawnPosition()
                .apply(player.getOriginServer())
        );
    }
}
