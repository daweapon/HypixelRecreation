package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import net.minestom.server.inventory.InventoryType;

public class GUITabWidgetLocations extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Tablist Widgets", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        TabLocation[] locations = TabLocation.values();
        for (int i = 0; i < locations.length && i < TabGui.GRID.length; i++) {
            TabLocation location = locations[i];
            layout.slot(TabGui.GRID[i], (s, c) -> ItemStacks.item(location.getIcon(),
                    "<a>" + location.getDisplayName() + "\n<7>Edit the widgets shown on\n<7>this island's tab list.\n\n<e>Click to edit!"),
                    (click, c) -> c.push(new GUITabWidgetList(location, 0)));
        }

        Components.backOrClose(layout, 40, ctx);
    }
}
