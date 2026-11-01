package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandSettings;
import dev.daweapon.type.skyblockgeneric.tab.TabSettings;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

public class GUISettings extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((state, context) -> "Settings", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(50, (s, c) -> ItemStacks.item(Material.BOOKSHELF,
                "<a>Tablist Widgets\n<7>View and manage Tablist Widget\n<7>settings.\n\n<e>Click to view!"),
                (click, c) -> c.push(new GUITabWidgetLocations()));

        layout.slot(14, (s, c) -> TabGui.toggle("Player List Info",
                "<7>Show the widget tab list. When off, the tab\n<7>list is just the plain player list.",
                TabSettings.of(c.player()).playerListInfo()), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            settings.setPlayerListInfo(!settings.playerListInfo());
            c.replace(new GUISettings());
        });

        layout.slot(32, (s, c) -> ItemStacks.item(Material.REPEATER,
                "<a>Island Settings\n<7>Edit SkyBlock settings regarding\n<7>your island.\n\n<b>Island settings are shared with your\n<b>co-op partners!\n\n<e>Click for more settings!"),
                (click, c) -> c.push(new GUIIslandSettings()));

        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }
}
