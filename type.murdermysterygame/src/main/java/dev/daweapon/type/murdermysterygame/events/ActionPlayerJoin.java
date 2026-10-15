package dev.daweapon.type.murdermysterygame.events;

import lombok.SneakyThrows;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.redis.service.GameInformationHandler;
import dev.daweapon.type.generic.utility.MathUtility;
import org.tinylog.Logger;

public class ActionPlayerJoin implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void run(AsyncPlayerConfigurationEvent event) {
        final MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();
        Logger.info("Player " + player.getUsername() + " joined the server from origin server " + player.getOriginServer());
        event.setSpawningInstance(HypixelConst.getEmptyInstance());
        player.setRespawnPoint(new Pos(0, 72, 0));

        MathUtility.delay(() -> tryJoinGame(player, false), 15);
    }

    private void tryJoinGame(MurderMysteryPlayer player, boolean isRetry) {
        if (!player.isOnline()) return;

        String preferredGameId = GameInformationHandler.game.remove(player.getUuid());
        if (preferredGameId == null) {
            if (!isRetry) {
                Logger.info("No game assignment found for " + player.getUsername() + ", retrying in 1 second...");
                MathUtility.delay(() -> tryJoinGame(player, true), 20);
                return;
            }
            Logger.error("Failed to find game assignment for player " + player.getUsername());
            player.sendMessage("<c>No game assignment found! Returning to lobby...");
            player.sendTo(ServerType.MURDER_MYSTERY_LOBBY);
            return;
        }

        Game preferred = TypeMurderMysteryGameLoader.getGameById(preferredGameId);
        if (preferred == null) {
            player.sendMessage("<c>The assigned game no longer exists! Returning to lobby...");
            player.sendTo(ServerType.MURDER_MYSTERY_LOBBY);
            return;
        }

        MathUtility.delay(() -> {
            if (!player.isOnline()) return;
            if (preferred.hasDisconnectedPlayer(player.getUuid())) {
                preferred.rejoin(player);
            } else {
                preferred.join(player);
            }
        }, 15);
    }
}
