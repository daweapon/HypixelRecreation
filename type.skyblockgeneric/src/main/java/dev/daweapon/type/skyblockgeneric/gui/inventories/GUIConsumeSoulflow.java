package dev.daweapon.type.skyblockgeneric.gui.inventories;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.SoulflowComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// GUI confirming consumption of every Soulflow item in the inventory into the player's Soulflow balance
public class GUIConsumeSoulflow implements View<GUIConsumeSoulflow.State> {

	public record State(SkyBlockItem item) {
		public State {
			if (!item.hasComponent(SoulflowComponent.class)) {
				throw new IllegalArgumentException("Item does not have SoulflowComponent");
			}
		}
	}

	@Override
	public ViewConfiguration<State> configuration() {
		return ViewConfiguration.translatable("gui_misc.consume_soulflow.title", InventoryType.CHEST_4_ROW);
	}

	@Override
	public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
		Components.fill(layout);

		layout.slot(13, (s, viewCtx) -> {
					SkyBlockPlayer player = (SkyBlockPlayer) viewCtx.player();
					int soulflow = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue();
					return ItemStacks.head("94f0c693b85658b0bae792c9f9b717eb024ab8c4b349455648ea08358b50ddc4", 1,
							Text.key("gui_misc.consume_soulflow.confirm_button"),
							Text.keyLines("gui_misc.consume_soulflow.confirm_button.lore", soulflow, inventorySoulflow(player)));
				},
				(click, viewCtx) -> {
					SkyBlockPlayer player = (SkyBlockPlayer) viewCtx.player();
					DatapointInteger datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class);
					int addition = 0;
					for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
						int amount = soulflowIn(player.getInventory().getItemStack(slot));
						if (amount == 0) continue;
						addition += amount;
						player.getInventory().setItemStack(slot, ItemStack.AIR);
					}
					if (addition > 0) {
						datapoint.setValue(datapoint.getValue() + addition);
						player.sendMessage(Text.key("gui_misc.consume_soulflow.consumed_message", addition, datapoint.getValue()));
					}
					player.closeInventory();
				}
		);
		Components.close(layout, 31);
	}

	private static int inventorySoulflow(SkyBlockPlayer player) {
		int total = 0;
		for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
			total += soulflowIn(player.getInventory().getItemStack(slot));
		}
		return total;
	}

	private static int soulflowIn(ItemStack stack) {
		if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) return 0;
		SkyBlockItem item = new SkyBlockItem(stack);
		if (!item.hasComponent(SoulflowComponent.class)) return 0;
		return item.getAmount() * item.getComponent(SoulflowComponent.class).getAmount();
	}

	public static void open(SkyBlockPlayer player, SkyBlockItem item) {
		player.openView(new GUIConsumeSoulflow(), new State(item));
	}

}
