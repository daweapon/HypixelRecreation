package dev.daweapon.type.galatea.gui;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUIAttributeTransfer extends StatelessView {
    private static final String INFO = """
            <c>Attribute Transfer
            <7>Do you still have old items
            <7>with greyed out Attributes on them?

            <7>You can use this Anvil to transfer
            <7>those attributes to the new system!""";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Attribute Transfer", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (int row = 0; row < 6; row++) {
            layout.slot(row * 9, ItemStacks.named(Material.RED_STAINED_GLASS_PANE, ""));
            layout.slot(row * 9 + 8, ItemStacks.named(Material.RED_STAINED_GLASS_PANE, ""));
        }
        layout.slot(22, ItemStacks.item(Material.ANVIL, INFO));
        layout.slot(31, ItemStacks.item(Material.BARRIER, INFO));
        Components.close(layout, 49);
    }
}
