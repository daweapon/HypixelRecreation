package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.potion.PotionEffect;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.game.GameStatus;
import dev.daweapon.type.murdermysterygame.protection.WeaknessProtectionManager;
import dev.daweapon.type.murdermysterygame.role.GameRole;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;

/**
 * Detects when the murderer is hit by a weakness splash potion.
 * When this happens, apply the "weakened" state to the murderer.
 */
public class ActionWeaknessSplash implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityPotionAddEvent event) {
        if (!(event.getEntity() instanceof MurderMysteryPlayer player)) return;

        // Only react to weakness effect
        if (event.getPotion().effect() != PotionEffect.WEAKNESS) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game == null || game.getGameStatus() != GameStatus.IN_PROGRESS) return;

        // Check if this player is the murderer
        GameRole role = game.getRoleManager().getRole(player.getUuid());
        if (role == GameRole.MURDERER) {
            WeaknessProtectionManager.applyWeaknessToMurderer(player);
        }
    }
}
