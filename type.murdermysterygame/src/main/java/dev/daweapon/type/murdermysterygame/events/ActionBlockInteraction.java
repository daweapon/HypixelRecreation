package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.game.GameStatus;
import dev.daweapon.type.murdermysterygame.maphandler.MapHandlerRegistry;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import org.tinylog.Logger;

public class ActionBlockInteraction implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        Point blockPos = event.getBlockPosition();

        // Log every block interaction to diagnose issues
        Logger.info("[MM-DEBUG] Block interact event fired at ({}, {}, {}), block: {}, player type: {}",
                blockPos.blockX(), blockPos.blockY(), blockPos.blockZ(),
                event.getBlock().name(), event.getPlayer().getClass().getSimpleName());

        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) {
            Logger.info("[MM-DEBUG] Player is not MurderMysteryPlayer, skipping");
            return;
        }

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) {
            Logger.info("[MM-DEBUG] Player not in a game");
            return;
        }
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            Logger.info("[MM-DEBUG] Game not in progress, status: {}", game.getGameStatus());
            return;
        }

        // Check if player is eliminated (spectator)
        if (player.isEliminated()) {
            Logger.info("[MM-DEBUG] Player is eliminated");
            event.setCancelled(true);
            return;
        }

        // Get map handler for this game's map
        String mapId = game.getMapEntry().getId();

        Logger.info("[MM-DEBUG] Looking for handler for map '{}'", mapId);

        MapHandlerRegistry.getHandler(mapId).ifPresentOrElse(
                handler -> {
                    Logger.info("[MM-DEBUG] Found handler, checking interaction");
                    if (handler.handleInteraction(event, player, game)) {
                        Logger.info("[MM-DEBUG] Interaction handled!");
                        event.setCancelled(true);
                    } else {
                        Logger.info("[MM-DEBUG] Interaction not matched");
                    }
                },
                () -> Logger.info("[MM-DEBUG] No handler found for map '{}'", mapId)
        );
    }
}
