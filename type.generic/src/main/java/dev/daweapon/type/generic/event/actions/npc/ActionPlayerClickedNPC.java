package dev.daweapon.type.generic.event.actions.npc;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.NPCConversations;
import dev.daweapon.type.generic.entity.npc.impl.NPCAnimalEntityImpl;
import dev.daweapon.type.generic.entity.npc.impl.NPCEntityImpl;
import dev.daweapon.type.generic.entity.npc.impl.NPCVillagerEntityImpl;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventHandler;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

public class ActionPlayerClickedNPC implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(PlayerEntityInteractEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

		if (event.getHand() != PlayerHand.MAIN) return;

		Entity entity = event.getTarget();
		HypixelNPC npc = HypixelNPC.getFromImpl(player, entity);
		if (npc == null) return;

		NPCInteractEvent clickEvent = new NPCInteractEvent(player, event.getHand(), npc);
		HypixelEventHandler.callCustomEvent(clickEvent);
		if (clickEvent.isCancelled()) return;

		NPCConversations conversations = NPCConversations.server();
		if (conversations.blocksClick(player.getUuid(), npc)) {
			player.sendMessage(NPCConversations.ALREADY_IN_CONVERSATION);
			return;
		}

		switch (entity) {
			case NPCEntityImpl _, NPCVillagerEntityImpl _, NPCAnimalEntityImpl _ -> {
				conversations.startClick(player.getUuid(), npc);
				try {
					npc.onClick(clickEvent);
				} finally {
					conversations.finishClick(player.getUuid());
				}
			}
			default -> {
				// This is not a NPC we can handle here
				Logger.warn("Player " + player.getUsername() + " clicked on an unknown NPC type: " + entity.getClass().getName());
			}
		}
	}
}
