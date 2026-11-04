package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.skin.GardenSkinType;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.List;

public class GUIGardenSkins extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Garden Skins"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 30, ctx);
        Components.close(layout, 31);

        layout.slot(11, (_, _) -> GardenMenus.item(Material.OAK_PLANKS, Text.of("<a>Barn Skins"), List.of(
                        Text.of("<7>View and select different skins for"),
                        Text.of("<7>your Barn."),
                        GardenMenus.blank(),
                        Text.of("<e>Click to view!"))),
                (_, c) -> GardenMenus.open(c, new GUIGardenSkinSelect(GardenSkinType.BARN)));
        layout.slot(15, (_, _) -> GardenMenus.item(Material.WHITE_STAINED_GLASS, Text.of("<a>Greenhouse Skins"), List.of(
                        Text.of("<7>View and select different skins for"),
                        Text.of("<7>your Greenhouse."),
                        GardenMenus.blank(),
                        Text.of("<e>Click to view!"))),
                (_, c) -> GardenMenus.open(c, new GUIGardenSkinSelect(GardenSkinType.GREENHOUSE)));
    }
}
