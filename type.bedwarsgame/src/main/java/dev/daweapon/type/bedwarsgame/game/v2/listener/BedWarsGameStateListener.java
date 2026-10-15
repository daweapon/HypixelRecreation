package dev.daweapon.type.bedwarsgame.game.v2.listener;

import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.game.game.event.GameStateChangeEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import org.tinylog.Logger;

// todo: move functionality here
// Logs Bedwars game state transitions (countdown/start/end); a placeholder for further logic.
public class BedWarsGameStateListener implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onGameStateChange(GameStateChangeEvent event) {
        BedWarsGame game = (BedWarsGame) event.game();
        if (game == null) return;

        Logger.info("BedWars game {} state changed: {} -> {}",
                event.game().getGameId(), event.previousState(), event.newState());

        if (event.isCountdownStart()) {
            onCountdownStarted(game);
        } else if (event.isGameStart()) {
            onGameStarted(game);
        } else if (event.isGameEnd()) {
            onGameEnded(game);
        }
    }

    private void onCountdownStarted(BedWarsGame game) {
        // Could update scoreboard, play sounds, etc.
    }

    private void onGameStarted(BedWarsGame game) {
        Logger.info("BedWars game {} has officially started with {} players",
            game.getGameId(), game.getPlayers().size());
    }

    private void onGameEnded(BedWarsGame game) {
        Logger.info("BedWars game {} has ended", game.getGameId());
    }
}
