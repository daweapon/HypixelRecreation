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
        return ViewConfiguration.withString((state, context) -> "Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);

        layout.slot(11, (s, c) -> ItemStacks.item(Material.BOOKSHELF,
                "<a>Tablist Widgets\n<7>Choose which widgets your tab list\n<7>shows on every island, and in what order.\n\n<e>Click to edit!"),
                (click, c) -> c.push(new GUITabWidgetLocations()));

        layout.slot(13, (s, c) -> TabGui.toggle("Player List Info",
                "<7>Show the widget tab list. When off, the tab\n<7>list is just the plain player list.",
                TabSettings.of(c.player()).playerListInfo()), (click, c) -> {
            TabSettings settings = TabSettings.of(c.player());
            settings.setPlayerListInfo(!settings.playerListInfo());
            c.replace(new GUISettings());
        });

        layout.slot(15, (s, c) -> ItemStacks.item(Material.GRASS_BLOCK,
                "<a>Island Settings\n<7>Manage your island's privacy, guests,\n<7>co-op and ignored players.\n\n<e>Click to open!"),
                (click, c) -> c.push(new GUIIslandSettings()));

        Components.backOrClose(layout, 31, ctx);
    }
}
