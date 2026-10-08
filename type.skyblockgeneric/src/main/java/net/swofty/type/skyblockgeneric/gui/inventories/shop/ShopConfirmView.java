package net.swofty.type.skyblockgeneric.gui.inventories.shop;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class ShopConfirmView implements View<ShopConfirmView.State> {

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((_, _) -> "Confirm", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(11, (s, _) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Buying: {}", s.item().getItem().getDisplayNameText()));
            lore.add(Text.of("<7>Cost:"));
            lore.addAll(s.item().getPrice().getGUIDisplay());
            return ItemStacks.item(Material.GREEN_TERRACOTTA, 1, Text.of("<a>Confirm"), lore);
        }, (_, c) -> {
            if (ShopView.purchase((SkyBlockPlayer) c.player(), state.item())) {
                c.navigator().pop();
            }
        });

        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, 1, Text.of("<c>Cancel"), List.of()),
                (_, c) -> c.navigator().pop());
    }

    public record State(ShopView.ShopItem item) {
    }
}
