package dev.daweapon.type.bedwarsgame.game.v2.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.bedwarsgame.TypeBedWarsGameLoader;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.game.game.event.GameDisposeEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

// Cleans up a disposed Bedwars game: stops managers and unregisters its instance once empty.
public class GameDisposalListener implements HypixelEventClass {
    private static final int MAX_UNREGISTER_ATTEMPTS = 120;

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onGameDispose(GameDisposeEvent event) {
        BedWarsGame disposedGame = (BedWarsGame) event.game();
        TypeBedWarsGameLoader.getGames().removeIf(game -> game.getGameId().equals(event.game().getGameId()));

        if (disposedGame != null) {
            disposedGame.getSwappageManager().stop();
            unregisterInstanceWhenEmpty(disposedGame, 0);
        }
    }

    private void unregisterInstanceWhenEmpty(BedWarsGame game, int attempts) {
        if (game.getInstance().getPlayers().isEmpty() || attempts >= MAX_UNREGISTER_ATTEMPTS) {
            MinecraftServer.getInstanceManager().unregisterInstance(game.getInstance());
            return;
        }

        MinecraftServer.getSchedulerManager().buildTask(() -> unregisterInstanceWhenEmpty(game, attempts + 1))
            .delay(TaskSchedule.millis(500))
            .schedule();
    }

}
