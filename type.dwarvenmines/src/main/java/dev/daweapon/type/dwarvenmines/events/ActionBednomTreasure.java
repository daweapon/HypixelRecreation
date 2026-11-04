package dev.daweapon.type.dwarvenmines.events;

import net.minestom.server.event.player.PlayerStartDiggingEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.bednom.BednomQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionBednomTreasure implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(PlayerStartDiggingEvent event) {
		if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
		if (!BednomQuest.isTreasure(event.getBlockPosition())) return;
		if (BednomQuest.digTreasure(player)) event.setCancelled(true);
	}
}
