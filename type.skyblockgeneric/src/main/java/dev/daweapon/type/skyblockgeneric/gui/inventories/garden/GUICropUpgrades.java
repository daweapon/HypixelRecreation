package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenConfig;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.GardenProgress;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUICropUpgrades extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_garden_upgrades.crops.title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        for (int i = 0; i < GardenGrids.CROP_ORDER.size(); i++) {
            GardenCrop crop = GardenGrids.CROP_ORDER.get(i);
            layout.slot(GardenGrids.CROP_SLOTS.get(i),
                    (_, c) -> cropItem(crop, GardenService.profile((SkyBlockPlayer) c.player())),
                    (_, c) -> GardenMenus.open(c, new GUICropTierUpgrades(crop)));
        }
    }

    private ItemStack.Builder cropItem(GardenCrop crop, GardenProfile profile) {
        GardenConfig config = GardenConfig.get();
        String name = crop.getDisplayName();
        return GardenMenus.item(GardenMenus.icon(crop), Text.key("gui_garden_upgrades.crops.name", name), List.of(
                Text.key("gui_garden_upgrades.crops.lore.1", name),
                Text.key("gui_garden_upgrades.crops.lore.2", name),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.crops.tier", profile.cropUpgradeTier(crop), config.maxUpgradeTier()),
                Text.key("gui_garden_upgrades.crops.fortune", name, GardenProgress.cropFortune(profile, crop)),
                GardenMenus.blank(),
                Text.key("gui_garden_upgrades.crops.click_to_view")));
    }
}
