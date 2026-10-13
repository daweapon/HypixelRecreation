package net.swofty.type.skyblockgeneric.gui.inventories.garden;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenCrop;
import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.garden.feast.FeastCalendar;
import net.swofty.type.skyblockgeneric.garden.feast.FeastData;
import net.swofty.type.skyblockgeneric.garden.feast.FeastService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
        layout.slot(15, (_, c) -> stew((SkyBlockPlayer) c.player()), (_, c) -> GardenMenus.open(c, new GUIFeastStew()));
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
