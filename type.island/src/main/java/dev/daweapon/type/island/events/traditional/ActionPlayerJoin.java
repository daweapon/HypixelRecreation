package dev.daweapon.type.island.events.traditional;

import lombok.SneakyThrows;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.island.TypeIslandLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Sets a player's spawning instance and respawn point when they connect to the Island server.
public class ActionPlayerJoin implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void run(AsyncPlayerConfigurationEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setSpawningInstance(HypixelConst.getEmptyInstance());

        player.setRespawnPoint(TypeIslandLoader.SPAWN_POS);
    }
}
