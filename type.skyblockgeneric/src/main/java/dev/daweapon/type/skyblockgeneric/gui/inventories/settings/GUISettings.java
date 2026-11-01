package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandSettings;
import dev.daweapon.type.skyblockgeneric.gui.inventories.islandsettings.GUIIslandSettingsPrivacy;
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

        for (SettingsCategory category : SettingsCategory.values()) {
            layout.slot(category.hubSlot(), (s, c) -> ItemStacks.item(category.hubMaterial(), category.hubText()),
                    (click, c) -> c.push(new GUISettingsCategory(category)));
        }

        layout.slot(32, (s, c) -> ItemStacks.item(Material.REPEATER,
                "<a>Island Settings\n<7>Edit SkyBlock settings regarding\n<7>your island.\n\n<b>Island settings are shared with your\n<b>co-op partners!\n\n<e>Click for more settings!"),
                (click, c) -> c.push(new GUIIslandSettingsPrivacy()));

        layout.slot(43, (s, c) -> ItemStacks.item(Material.GRASS_BLOCK,
                "<a>Island Management\n<7>View and manage island settings!\n\n<e>Click to view!"),
                (click, c) -> c.push(new GUIIslandSettings()));

        layout.slot(50, (s, c) -> ItemStacks.item(Material.BOOKSHELF,
                "<a>Tablist Widgets\n<7>View and manage Tablist Widget\n<7>settings.\n\n<e>Click to view!"),
                (click, c) -> c.push(new GUITabWidgetLocations()));

        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }
}
