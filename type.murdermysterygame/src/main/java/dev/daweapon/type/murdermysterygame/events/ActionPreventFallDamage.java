package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.damage.DamageType;
import dev.daweapon.pvp.events.FinalDamageEvent;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPreventFallDamage implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(FinalDamageEvent event) {
        if (!(event.getEntity() instanceof MurderMysteryPlayer player)) return;

        // Allow damage in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null) return;

        // Check if this is fall damage
        DamageType damageType = event.getDamage().getType().asValue();
        if (damageType == DamageType.FALL.asValue()) {
            event.setCancelled(true);
        }
    }
}
