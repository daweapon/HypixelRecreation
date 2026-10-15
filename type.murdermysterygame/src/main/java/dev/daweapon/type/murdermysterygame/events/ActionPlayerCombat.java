package dev.daweapon.type.murdermysterygame.events;

import net.minestom.server.item.Material;
import dev.daweapon.pvp.events.PrepareAttackEvent;
import dev.daweapon.type.murdermysterygame.TypeMurderMysteryGameLoader;
import dev.daweapon.type.murdermysterygame.game.Game;
import dev.daweapon.type.murdermysterygame.game.GameStatus;
import dev.daweapon.type.murdermysterygame.protection.WeaknessProtectionManager;
import dev.daweapon.type.murdermysterygame.role.GameRole;
import dev.daweapon.type.murdermysterygame.user.MurderMysteryPlayer;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;

public class ActionPlayerCombat implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PrepareAttackEvent event) {
        if (!(event.getEntity() instanceof MurderMysteryPlayer attacker)) return;
        if (!(event.getTarget() instanceof MurderMysteryPlayer victim)) return;

        Game game = TypeMurderMysteryGameLoader.getPlayerGame(attacker);
        if (game == null) {
            event.setCancelled(true);
            return;
        }

        // No combat in waiting lobby
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        // Check if victim is already eliminated
        if (victim.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        // Check if attacker is eliminated (spectator)
        if (attacker.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        GameRole attackerRole = game.getRoleManager().getRole(attacker.getUuid());
        if (attackerRole == null) {
            event.setCancelled(true);
            return;
        }

        // Only murderer with knife can melee kill
        if (attackerRole == GameRole.MURDERER) {
            // Check if holding knife (iron sword) and murderer has received their sword
            Material heldItem = attacker.getItemInMainHand().material();
            if (heldItem == Material.IRON_SWORD && game.hasMurdererReceivedSword()) {
                // Check if murderer is weakened (from weakness splash potion)
                if (!WeaknessProtectionManager.handleMurdererAttack(attacker)) {
                    // Murderer is weakened - attack absorbed
                    attacker.sendMessage("<e>Your attack was weakened!");
                    event.setCancelled(true);
                    return;
                }
                // Kill the victim
                game.onPlayerKill(attacker, victim);
            }
            // Cancel the attack either way - murderer either kills instantly or does nothing
            event.setCancelled(true);
        } else {
            // Innocents and detectives cannot melee kill - they use bow
            // Cancel regular melee attacks
            event.setCancelled(true);
        }
    }
}
