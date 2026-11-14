package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.Material;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.game.GameStatus;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionGoldDrop implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(ItemDropEvent event) {
        if (!(event.getPlayer() instanceof MurderMysteryPlayer player)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        Material material = event.getItemStack().material();

        if (material == Material.GOLD_INGOT) {
            event.setCancelled(true);
            return;
        }

        // Prevent dropping weapons
        if (material == Material.BOW || material == Material.ARROW || material == Material.IRON_SWORD) {
            event.setCancelled(true);
        }
    }
}
