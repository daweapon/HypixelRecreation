package dev.daweapon.type.skyblockgeneric.gui.inventories;

import net.minestom.server.inventory.InventoryType;
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

// GUI confirming consumption of a Soulflow item to add to the player's Soulflow balance
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
					SkyBlockDataHandler data = player.getSkyblockDataHandler();
					int soulflow = data.get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue();
					int itemSoulflow = s.item().getComponent(SoulflowComponent.class).getAmount();
					int addition = s.item().getAmount() * itemSoulflow;

                return ItemStacks.head("94f0c693b85658b0bae792c9f9b717eb024ab8c4b349455648ea08358b50ddc4", 1,
                    Text.key("gui_misc.consume_soulflow.confirm_button"),
                    Text.keyLines("gui_misc.consume_soulflow.confirm_button.lore", soulflow, addition));
				},
				(click, viewCtx) -> {
					SkyBlockPlayer player = (SkyBlockPlayer) viewCtx.player();
					SkyBlockDataHandler data = player.getSkyblockDataHandler();
					int soulflow = data.get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue();

					int itemSoulflow = click.state().item().getComponent(SoulflowComponent.class).getAmount();
					int addition = click.state().item().getAmount() * itemSoulflow;

					data.get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).setValue(soulflow + addition);
                    player.sendMessage(Text.key("gui_misc.consume_soulflow.consumed_message", addition, soulflow + addition));

					player.getInventory().setItemStack(player.getHeldSlot(), ItemStack.AIR);
					player.closeInventory();
				}
		);
		Components.close(layout, 31);
	}

	public static void open(SkyBlockPlayer player, SkyBlockItem item) {
		player.openView(new GUIConsumeSoulflow(), new State(item));
	}

}
