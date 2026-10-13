package net.swofty.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionPassCheck implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(PlayerSpawnEvent event) {
		if (!event.isFirstSpawn()) return;
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (CrystalHollowsAccess.hasValidPass(player)) return;
		player.sendMessage("<c>You have been teleported out of the Crystal Hollows because you did not have a valid pass!");
		player.sendTo(ServerType.SKYBLOCK_DWARVEN_MINES);
	}
}
