package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenService;
import net.swofty.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUISkyMart extends StatelessView {
    private static final String KEY = "gui_skymart.";

    private record Category(int slot, String id, Material icon, String shop, int requiredLevel) {
    }

    private static final List<Category> CATEGORIES = List.of(
            new Category(11, "essentials", Material.WHEAT, "skymart_farming_essentials", 0),
            new Category(12, "tools", Material.DIAMOND_HOE, "skymart_farming_tools", 4),
            new Category(13, "barn", Material.OAK_DOOR, "skymart_barn_skins", 0),
            new Category(14, "greenhouse", Material.GLASS, "skymart_greenhouse_skins", 0),
            new Category(15, "pests", Material.SPIDER_EYE, "skymart_pests", 0));

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key(KEY + "title"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 31, ctx);

        for (Category category : CATEGORIES) {
            layout.slot(category.slot(), (_, c) -> {
                int level = GardenService.level((SkyBlockPlayer) c.player());
                List<Text> lore = new ArrayList<>(GardenMenus.lines(KEY + category.id() + ".lore.", 1));
                lore.add(GardenMenus.blank());
                lore.add(level >= category.requiredLevel()
                        ? Text.key(KEY + "click")
                        : Text.key(KEY + "locked", category.requiredLevel()));
                return GardenMenus.item(category.icon(), Text.key(KEY + category.id() + ".name"), lore);
            }, (_, c) -> open(category, c));
        }
    }

    private void open(Category category, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (GardenService.level(player) < category.requiredLevel()) {
            player.sendMessage(Text.key(KEY + "locked", category.requiredLevel()));
            return;
        }
        GardenMenus.open(ctx, new CatalogueShopView(category.shop()));
    }
}
