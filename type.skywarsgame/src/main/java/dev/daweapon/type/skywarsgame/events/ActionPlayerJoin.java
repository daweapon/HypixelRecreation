package dev.daweapon.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.redis.service.GameInformationHandler;
import dev.daweapon.type.generic.utility.MathUtility;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.game.SkywarsGameStatus;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import org.tinylog.Logger;

public class ActionPlayerJoin implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.CONNECT)
    public void onJoin(AsyncPlayerConfigurationEvent event) {
        SkywarsPlayer player = (SkywarsPlayer) event.getPlayer();
        Logger.info("Player " + player.getUsername() + " joined the server from origin server " + player.getOriginServer());

        event.setSpawningInstance(HypixelConst.getEmptyInstance());
        player.setRespawnPoint(new Pos(0, 100, 0));

        MathUtility.delay(() -> tryJoinGame(player, false), 15);
    }

    private void tryJoinGame(SkywarsPlayer player, boolean isRetry) {
        if (!player.isOnline()) return;

        String assignedGameId = GameInformationHandler.game.remove(player.getUuid());
        if (assignedGameId == null) {
            if (!isRetry) {
                Logger.info("No game assignment found for " + player.getUsername() + ", retrying in 1 second...");
                MathUtility.delay(() -> tryJoinGame(player, true), 20);
                return;
            }
            player.sendMessage("<c>No game assignment found! Returning to lobby...");
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return;
        }

        SkywarsGame assignedGame = TypeSkywarsGameLoader.getGameById(assignedGameId);
        if (assignedGame == null) {
            player.sendMessage("<c>The assigned game no longer exists! Returning to lobby...");
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return;
        }

        if (assignedGame.getGameStatus() != SkywarsGameStatus.WAITING) {
            player.sendMessage("<c>The game has already started! Returning to lobby...");
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return;
        }

        if (assignedGame.getPlayers().size() >= assignedGame.getGameType().getMaxPlayers()) {
            player.sendMessage("<c>The game is full! Returning to lobby...");
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return;
        }

        assignedGame.join(player);
    }
}
