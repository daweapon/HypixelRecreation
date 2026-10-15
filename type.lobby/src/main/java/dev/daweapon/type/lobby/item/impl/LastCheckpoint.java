package dev.daweapon.type.lobby.item.impl;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.lobby.LobbyTypeLoader;
import dev.daweapon.type.lobby.item.LobbyItem;
import dev.daweapon.type.lobby.parkour.LobbyParkourManager;

public class LastCheckpoint extends LobbyItem {

	public LastCheckpoint() {
		super("last_checkpoint");
	}

	@Override
	public ItemStack getBlandItem() {
		return ItemStacks.named(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, "<a>Teleport to Last Checkpoint").build();
	}

	@Override
	public void onItemInteract(PlayerInstanceEvent event) {
		((CancellableEvent) event).setCancelled(true);
		if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
			LobbyParkourManager parkourManager = lobbyLoader.getParkourManager();
			if (parkourManager == null) return;
			event.getPlayer().teleport(
					parkourManager.getParkour().getCheckpoints().get(parkourManager.getPerPlayerStartTime().get(event.getPlayer().getUuid()).lastCheckpointIndex()).asPos().add(0.5, 0, 0.5)
			);
		}
	}
}
