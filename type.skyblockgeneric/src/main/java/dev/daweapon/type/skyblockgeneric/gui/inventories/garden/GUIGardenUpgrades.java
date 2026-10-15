package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

import java.util.List;

public class GUIGardenUpgrades extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_garden_upgrades.hub.title"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 30, ctx);
        Components.close(layout, 31);

        layout.slot(11, GardenMenus.item(Material.WHEAT, Text.key("gui_garden_upgrades.hub.crop.name"), List.of(
                Text.key("gui_garden_upgrades.hub.crop.lore.1"),
                Text.key("gui_garden_upgrades.hub.crop.lore.2"),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.hub.click_to_view"))),
                (_, c) -> GardenMenus.open(c, new GUICropUpgrades()));

        layout.slot(13, GardenMenus.item(Material.COMPOSTER, Text.key("gui_garden_upgrades.hub.composter.name"), List.of(
                Text.key("gui_garden_upgrades.hub.composter.lore.1"),
                Text.key("gui_garden_upgrades.hub.composter.lore.2"),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.hub.click_to_view"))),
                (_, c) -> c.player().sendMessage(Text.key("gui_garden_upgrades.hub.composter.unavailable")));

        layout.slot(15, GardenMenus.item(Material.WHITE_CONCRETE, Text.key("gui_garden_upgrades.hub.greenhouse.name"), List.of(
                Text.key("gui_garden_upgrades.hub.greenhouse.lore.1"),
                Text.key("gui_garden_upgrades.hub.greenhouse.lore.2"),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.hub.click_to_view"))),
                (_, c) -> c.player().sendMessage(Text.key("gui_garden_upgrades.hub.greenhouse.unavailable")));
    }
}
