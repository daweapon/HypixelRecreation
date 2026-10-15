package dev.daweapon.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.crystalhollows.lifecycle.CrystalHollowsLobby;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsAccess;
import dev.daweapon.type.skyblockgeneric.crystalhollows.HollowsSpawnProtection;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
