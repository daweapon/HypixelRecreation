package dev.daweapon.type.replayviewer.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.replayviewer.TypeReplayViewerLoader;
import dev.daweapon.type.replayviewer.item.ReplayItem;
import dev.daweapon.type.replayviewer.playback.ReplaySession;

public class SlowerItem extends ReplayItem {

	public SlowerItem() {
		super("slower");
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStacks.head("dcd7c14b92cb37909208a0d204780493f9c9cc5f56d1019b7363417909f1d956", "<a>Decrease Speed").build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		((CancellableEvent) event).setCancelled(true);
		HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		TypeReplayViewerLoader.getSession(player).ifPresentOrElse(
			ReplaySession::cycleSpeedDown,
			() -> player.sendMessage("<c>No active replay session.")
		);
	}
}
