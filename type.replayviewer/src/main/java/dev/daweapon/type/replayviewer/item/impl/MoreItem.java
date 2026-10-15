package dev.daweapon.type.replayviewer.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.replayviewer.item.ReplayItem;
import dev.daweapon.type.replayviewer.view.GUIReplayViewer;

public class MoreItem extends ReplayItem {

	public MoreItem() {
		super("menu");
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStacks.named(Material.NETHER_STAR, "<e>More").build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
		player.openView(new GUIReplayViewer());
	}
}
