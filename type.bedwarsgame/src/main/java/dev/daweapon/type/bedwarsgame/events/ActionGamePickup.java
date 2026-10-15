package dev.daweapon.type.bedwarsgame.events;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import dev.daweapon.type.bedwarsgame.game.v2.BedWarsGame;
import dev.daweapon.type.bedwarsgame.shop.Currency;
import dev.daweapon.type.bedwarsgame.user.BedWarsPlayer;
import dev.daweapon.type.bedwarsgame.user.ExperienceCause;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import org.jetbrains.annotations.Nullable;

// Handles item pickups in Bedwars, awarding currency XP and recording pickups for replay.
public class ActionGamePickup implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false)
	public void run(PickupItemEvent event) {
		ItemStack itemStack = event.getItemEntity().getItemStack();
		if (event.getLivingEntity() instanceof BedWarsPlayer player) {
			BedWarsGame game = player.getGame();
			if (!game.isPlayerCurrentlyPlaying(player.getUuid())) {
				event.setCancelled(true);
				return;
			}

			// Record item pickup to replay
			if (game != null && game.getReplayManager().isRecording()) {
				game.getReplayManager().recordItemPickup(
					event.getItemEntity().getEntityId(),
					player.getEntityId()
				);
				game.getReplayManager().recordEntityDespawn(event.getItemEntity().getEntityId());
			}

			player.getInventory().addItemStack(itemStack);

			// handle bedwars xp for diamonds and emeralds
			for (Currency currency : Currency.values()) {
				if (itemStack.material().equals(currency.getMaterial())) {
					@Nullable CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
					if (data != null && !data.nbt().getBoolean("generator")) continue;
					int amount = itemStack.amount();
					switch (currency) {
						case DIAMOND:
							player.xp(ExperienceCause.DIAMONDS, amount);
							break;
						case EMERALD:
							player.xp(ExperienceCause.EMERALDS, amount);
							break;
					}
					break;
				}
			}
		}
	}

}
