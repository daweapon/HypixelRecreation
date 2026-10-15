package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPreventBlockModification implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockBreak(PlayerBlockBreakEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        // Allow in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game != null) {
            event.setCancelled(true);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockPlace(PlayerBlockPlaceEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        // Allow in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game != null) {
            event.setCancelled(true);
        }
    }
}
