package dev.daweapon.type.hub.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.museum.MuseumDisplays;
import dev.daweapon.type.skyblockgeneric.museum.MuseumService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Refreshes a player's museum item displays shortly after they first spawn in the Hub.
public class ActionPlayerDisplayMuseum implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!event.isFirstSpawn()) return;

        CompletableFuture.delayedExecutor(2000, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    if (!player.isOnline()) return;
                    MuseumService.sync(player);
                    MuseumDisplays.updateDisplay(player);
                });
    }
}
