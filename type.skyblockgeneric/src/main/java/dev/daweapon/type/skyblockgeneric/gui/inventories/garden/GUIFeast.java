package dev.daweapon.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastCalendar;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastData;
import dev.daweapon.type.skyblockgeneric.garden.feast.FeastService;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIFeast extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Harvest Feast"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        layout.slot(11, (_, _) -> seasonItem());
        layout.slot(13, (_, _) -> bakery(), (_, c) -> openBakery(c));
        layout.slot(15, (_, c) -> stew((SkyBlockPlayer) c.player()), (_, c) -> GardenMenus.open(c, new GUIFeastStew()));
    }

    private ItemStack.Builder bakery() {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Spend <e>Kernels <7>on baked goods."));
        lore.add(Text.empty());
        lore.add(FeastCalendar.grand() ? Text.of("<e>Click to browse!") : Text.of("<c>Only open during the Grand Feast."));
        return ItemStacks.item(Material.BREAD, Text.of("<6>Grand Bakery"), lore);
    }

    private void openBakery(ViewContext ctx) {
        if (!FeastCalendar.grand()) {
            ctx.player().sendMessage(Text.of("<c>The Grand Bakery is only open during the Grand Feast!"));
            return;
        }
        GardenMenus.open(ctx, new CatalogueShopView("grand_bakery"));
    }

    private ItemStack.Builder seasonItem() {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(FeastCalendar.active() ? FeastCalendar.grand() ? "<d>The Grand Feast is on!" : "<6>The Harvest Feast is on!" : "<c>The Harvest Feast is not on right now."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>In-Season Crops <8>(rotate in {}h)", Math.round(FeastCalendar.hoursUntilRotation())));
        for (GardenCrop crop : FeastCalendar.inSeasonNow()) lore.add(Text.of("<8>• <a>{}", crop.getDisplayName()));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Next month:"));
        for (GardenCrop crop : FeastCalendar.inSeasonNext()) lore.add(Text.of("<8>• <7>{}", crop.getDisplayName()));
        return ItemStacks.item(Material.WHEAT, Text.of("<a>In-Season Crops"), lore);
    }

    private ItemStack.Builder stew(SkyBlockPlayer player) {
        GardenProfile profile = Gardens.profile(player);
        FeastData data = FeastService.data(profile);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Seasoning you find while farming is"));
        lore.add(Text.of("<7>donated to the Communal Stew."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Seasoning: <a>{}", StringUtility.commaify(data.getSeasoning())));
        if (FeastCalendar.grand()) lore.add(Text.of("<7>Kernels: <e>{}", StringUtility.commaify(data.getKernels())));
        lore.add(Text.of("<7>Milestones claimed: <a>{}<7>/<a>{}", data.getClaimed(), FeastService.targets().length));
        lore.add(Text.empty());
        lore.add(FeastService.claimable(profile) ? Text.of("<e>Click to view and claim rewards!") : Text.of("<e>Click to view donation tiers!"));
        return ItemStacks.item(Material.BREAD, Text.of("<6>Communal Stew"), lore);
    }
}
