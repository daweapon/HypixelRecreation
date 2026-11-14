package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.game.GameStatus;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;

/**
 * Kills players who fall into water with a drowning death message.
 */
public class ActionWaterDeath implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) return;

        // Already eliminated
        if (player.isEliminated()) return;

        // Check block at player's feet position
        Pos pos = player.getPosition();
        Block blockAtFeet = game.getInstanceContainer().getBlock(pos.blockX(), pos.blockY(), pos.blockZ());

        // Check if player is in water
        if (blockAtFeet == Block.WATER) {
            game.onEnvironmentalDeath(player, "You drowned.");
        }
    }
}
