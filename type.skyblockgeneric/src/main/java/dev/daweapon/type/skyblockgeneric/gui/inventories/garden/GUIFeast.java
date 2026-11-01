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
import java.util.Locale;

public class GUIFeast extends StatelessView {
    private static final int[] SEASON_SLOTS = {11, 12, 14, 15};
    private static final int[] MILESTONE_SLOTS = {29, 30, 31, 32, 33, 38, 39, 40, 41};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Harvest Feast"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(48, (_, _) -> bakery(), (_, c) -> openBakery(c));
        layout.slot(50, (_, _) -> ItemStacks.item(Material.GOLDEN_HOE, Text.of("<6>All Crops"), List.of(
                        Text.of("<7>See a list of all crops and when"),
                        Text.of("<7>they'll be in-season next!"),
                        Text.empty(),
                        Text.of("<e>Click to view!"))),
                (_, c) -> GardenMenus.open(c, new GUIFeastAllCrops()));

        List<GardenCrop> inSeason = FeastCalendar.inSeasonNow();
        for (int i = 0; i < SEASON_SLOTS.length && i < inSeason.size(); i++) {
            GardenCrop crop = inSeason.get(i);
            layout.slot(SEASON_SLOTS[i], (_, _) -> cropItem(crop, true));
        }

        int tiers = Math.min(MILESTONE_SLOTS.length, FeastService.targets().length);
        for (int tier = 0; tier < tiers; tier++) {
            int index = tier;
            layout.slot(MILESTONE_SLOTS[tier], (_, c) -> tierItem((SkyBlockPlayer) c.player(), index), (_, c) -> click(c, index));
        }
    }

    static ItemStack.Builder cropItem(GardenCrop crop, boolean inSeason) {
        String rare = FeastCalendar.rareCrop(crop);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(inSeason ? "<8>In Season" : "<8>Out of Season"));
        lore.add(Text.empty());
        lore.add(Text.of("<6><bold>RARE CROPS"));
        lore.add(Text.of("<7>- <2>Seasoning <8>(<a>1<8>/<a>{}<8>)",
                StringUtility.commaify(Math.round(100 / FeastService.SEASONING_PERCENT))));
        if (rare != null) {
            lore.add(Text.of("<7>- <a>{} <8>(<a>1<8>/<a>{}<8>)", title(rare),
                    StringUtility.commaify(Math.round(100 / FeastService.HARVEST_RARE_CROP_PERCENT))));
        }
        lore.add(Text.empty());
        double hours = FeastCalendar.hoursUntilRotation();
        lore.add(inSeason
                ? Text.of("<e>Will be in-season for another {}h {}m!", (int) hours, (int) ((hours - (int) hours) * 60))
                : Text.of("<c>Out of season until the next Feast!"));
        String name = crop == GardenCrop.MELON ? "Melon" : crop == GardenCrop.MUSHROOM ? "Mushrooms" : crop.getDisplayName();
        return ItemStacks.item(GardenMenus.icon(crop), Text.of((inSeason ? "<a>" : "<c>") + name), lore);
    }

    private static String title(String id) {
        StringBuilder name = new StringBuilder();
        for (String word : id.toLowerCase(Locale.ROOT).split("_")) {
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
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

    private ItemStack.Builder tierItem(SkyBlockPlayer player, int tier) {
        GardenProfile profile = Gardens.profile(player);
        FeastData data = FeastService.data(profile);
        int target = FeastService.targets()[tier];
        boolean grand = FeastCalendar.grand();
        boolean last = tier == FeastService.targets().length - 1;
        String roman = StringUtility.getAsRomanNumeral(tier + 1);
        boolean claimed = data.getClaimed() > tier;
        boolean reached = data.getSeasoning() >= target;
        boolean next = data.getClaimed() == tier;
        int into = Math.min(data.getSeasoning(), target);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Farm crops that are in-season to"));
        lore.add(Text.of("<7>earn <2>Seasoning<7>! It'll automatically"));
        lore.add(Text.of("<7>get donated to the <e>Communal Stew<7>."));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Progress: <e>{}<6>%", GardenMenus.percent(into, target)));
        int filled = GardenMenus.filled(into, target, GardenMenus.BAR_WIDTH);
        lore.add(Text.of("<dark_green><m>" + GardenMenus.spaces(filled) + "<white><m>" + GardenMenus.spaces(GardenMenus.BAR_WIDTH - filled)
                + "<reset> <e>{}<6>/<e>{} Donations", StringUtility.commaify(into), StringUtility.commaify(target)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Rewards:"));
        int flasks = FeastService.flasksFor(player, tier);
        lore.add(flasks > 1 ? Text.of("<7>- <5>Feast Flask <8>x{}", flasks) : Text.of("<7>- <5>Feast Flask"));
        if (last && !grand) lore.add(Text.of("<7>- <c>Commemorative Mason Jar"));
        if (grand) lore.add(Text.of("<7>- <e>{} Kernels", FeastService.GRAND_KERNELS[tier]));
        lore.add(Text.of("<7>- <a>Carnival Ticket <8>x{}", FeastService.TICKETS_PER_MILESTONE));

        if (claimed) {
            lore.add(Text.empty());
            lore.add(Text.of("<a>Reward claimed!"));
            return ItemStacks.item(Material.LIME_STAINED_GLASS_PANE, tier + 1, Text.of("<a>Feast Milestone " + roman), lore);
        }
        if (reached && next) {
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to claim!"));
            return ItemStacks.item(Material.YELLOW_STAINED_GLASS_PANE, tier + 1, Text.of("<e>Feast Milestone " + roman), lore);
        }
        return ItemStacks.item(next ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, tier + 1,
                Text.of((next ? "<e>" : "<c>") + "Feast Milestone " + roman), lore);
    }

    private void click(ViewContext ctx, int tier) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        FeastData data = FeastService.data(Gardens.profile(player));
        if (tier < data.getClaimed()) return;
        if (tier > data.getClaimed()) {
            player.sendMessage(Text.of("<c>Claim the previous tier first!"));
            return;
        }
        if (!FeastService.claim(player)) player.sendMessage(Text.of("<c>You haven't donated enough Seasoning for this tier yet!"));
        ctx.session(DefaultState.class).refresh();
    }
}
