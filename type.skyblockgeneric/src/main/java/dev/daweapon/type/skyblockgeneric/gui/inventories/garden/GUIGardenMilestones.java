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

import java.util.ArrayList;
import java.util.List;

public class GUIGardenMilestones extends StatelessView {
    private static final String KEY = "gui_garden_desk.hub.";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key(KEY + "title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        if (!Components.back(layout, 48, ctx)) {
            Components.close(layout, 48);
        }
        Components.close(layout, 49);

        layout.slot(21, (_, _) -> GardenMenus.item(Material.WHEAT, Text.key(KEY + "crops.name"), lore("crops")),
                (_, c) -> GardenMenus.open(c, new GUICropMilestones()));
        layout.slot(23, (_, _) -> GardenMenus.item(Material.PLAYER_HEAD, Text.key(KEY + "visitors.name"), lore("visitors")),
                (_, c) -> GardenMenus.open(c, new GUIVisitorMilestones()));
    }

    private List<Text> lore(String id) {
        List<Text> lore = new ArrayList<>(GardenMenus.lines(KEY + id + ".lore.", id.equals("visitors") ? 3 : 2));
        lore.add(GardenMenus.blank());
        lore.add(Text.key("gui_garden_desk.click_to_view"));
        return lore;
    }
}
