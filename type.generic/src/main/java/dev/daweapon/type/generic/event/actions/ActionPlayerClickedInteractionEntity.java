package dev.daweapon.type.generic.event.actions;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class ActionPlayerClickedInteractionEntity implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(PlayerEntityInteractEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		if (event.getHand() != PlayerHand.MAIN) return;
		if (event.getTarget() instanceof InteractionEntity interaction) {
			interaction.onClick.accept(player, event);
		}
	}
}
