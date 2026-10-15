package dev.daweapon.type.bedwarsgame.events;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.entity.EntityDamageEvent;
import dev.daweapon.pvp.events.PrepareAttackEvent;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.item.impl.LuckyCombatEffects;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.game.game.GameState;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;

// Guards attacks in Bedwars: blocks hits on NPCs/teammates and applies lucky combat effects.
public class ActionEntityAttack implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false)
	public void run(PrepareAttackEvent event) {
		if (event.getEntity() instanceof BedWarsPlayer player) {
			BedWarsGame game = player.getGame();
			if (game == null) {
				event.setCancelled(true);
				return;
			}

			if (game.getState() != GameState.IN_PROGRESS) {
				event.setCancelled(true);
				return;
			}

			for (Entity entity : HypixelNPC.getPerPlayerNPCs().get(player.getUuid()).getEntityImpls().values()) {
				if (event.getTarget() == entity) {
					event.setCancelled(true);
					return;
				}
			}

			if (game.isSameTeam(player.getUuid(), event.getTarget().getUuid())) {
				player.sendMessage("<c>You cannot attack your teammate!");
				event.setCancelled(true);
				return;
			}

			LuckyCombatEffects.handle(player, event.getTarget());
		}
	}

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
	public void run(EntityDamageEvent event) {
		if (event.getEntity() instanceof BedWarsPlayer player) {
			BedWarsGame game = player.getGame();
			if (game == null) {
				return;
			}

			if (game.getState() == GameState.IN_PROGRESS) {
				player.updateBelowTag();
			}
		}
	}

}
