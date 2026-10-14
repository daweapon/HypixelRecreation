package net.swofty.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.essence.CataloguePerk;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.essence.EssenceShop;
import net.swofty.type.skyblockgeneric.essence.EssenceShops;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.gui.ShopView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.CombinedShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.EssenceShopPrice;
import net.swofty.type.skyblockgeneric.shop.type.ItemShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

// The perk shop of any data driven essence (everything except Ice, Crimson and Fossil).
public class GUIEssenceShop extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    private static final int[] DIAMOND_SLOTS = {19, 20, 21, 22, 23, 24};
    private static final int PERFECTLY_CUT_DIAMOND_SLOT = 25;
    private static final int MINING_OFF_CAMERA_SLOT = 28;
    private static final ShopPrice PERFECTLY_CUT_DIAMOND_PRICE = new CombinedShopPrice(List.of(
            new EssenceShopPrice(EssenceType.DIAMOND, 1_000), new ItemShopPrice(ItemType.REFINED_DIAMOND, 2)));
    private static final ShopPrice MINING_OFF_CAMERA_PRICE = new EssenceShopPrice(EssenceType.DIAMOND, 2_500);

    private final EssenceType essence;

    public GUIEssenceShop(EssenceType essence) {
        this.essence = essence;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(essence.getDisplayName() + " Essence Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        EssenceGuideEntry entry = EssenceGuideEntry.of(essence);
        layout.slot(4, (_, c) -> ItemStacks.head(entry.texture(), """
                <a>{} Essence Shop
                <7>Exchange {}{} Essence <7>for {}

                <7>Your {} Essence: <d>{}""", essence.getDisplayName(), entry.colour(), essence.getDisplayName(),
                essence == EssenceType.DIAMOND ? "perks useful in the <5>Crystal Hollows <7>and beyond!" : "perks!",
                essence.getDisplayName(), StringUtility.commaify(Essences.get((SkyBlockPlayer) c.player(), essence))));
        if (essence == EssenceType.DIAMOND) {
            layout.slot(50, ItemStacks.item(Material.REDSTONE_TORCH, """
                    <a>Essence Guide
                    <7>You can use <d>Essence <7>to upgrade
                    <7>items or convert them into <b>Dungeon
                    <b>items<7>.

                    <7>This guide shows the different
                    <7>Essences you can obtain and which
                    <7>items can be upgraded using each
                    <7>one.

                    <e>Click to view!"""), (_, c) -> c.push(new GUIEssenceGuide()));
            purchasable(layout, PERFECTLY_CUT_DIAMOND_SLOT, ItemType.PERFECTLY_CUT_DIAMOND, PERFECTLY_CUT_DIAMOND_PRICE);
            purchasable(layout, MINING_OFF_CAMERA_SLOT, ItemType.MINING_OFF_CAMERA, MINING_OFF_CAMERA_PRICE);
        }
        EssenceShop<CataloguePerk> shop = EssenceShops.shop(essence).orElseThrow();
        List<CataloguePerk> perks = shop.perks();
        int[] slots = essence == EssenceType.DIAMOND ? DIAMOND_SLOTS : SLOTS;
        for (int i = 0; i < perks.size() && i < slots.length; i++) {
            CataloguePerk perk = perks.get(i);
            layout.slot(slots[i], (_, c) -> perkItem((SkyBlockPlayer) c.player(), perk), (_, c) -> {
                buy(shop, (SkyBlockPlayer) c.player(), perk);
                c.session(DefaultState.class).refresh();
            });
        }
    }

    private void purchasable(ViewLayout<DefaultState> layout, int slot, ItemType type, ShopPrice price) {
        ShopView.ShopItem offer = new ShopView.ShopItem(new SkyBlockItem(type), 1, price, false, false);
        layout.slot(slot, (_, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            ItemStack.Builder stack = PlayerItemUpdater.playerUpdate(player, offer.getItem().getItemStackBuilder().build());
            List<Text> lore = new ArrayList<>();
            lore.add(Text.empty());
            lore.add(Text.of("<7>Cost"));
            lore.addAll(price.getGUIDisplay());
            lore.add(Text.empty());
            lore.add(Text.of(price.canAfford(player) ? "<e>Click to purchase!" : "<c>You don't have enough " + price.getNamePlural() + "!"));
            return ItemStacks.appendLore(stack, lore);
        }, (_, c) -> {
            ShopView.purchase((SkyBlockPlayer) c.player(), new ShopView.ShopItem(new SkyBlockItem(type), 1, price, false, false));
            c.session(DefaultState.class).refresh();
        });
    }

    private ItemStack.Builder perkItem(SkyBlockPlayer player, CataloguePerk perk) {
        int owned = EssencePerks.tier(player, perk);
        boolean maxed = owned >= perk.getMaxTier();
        int shown = maxed ? owned : owned + 1;
        boolean available = perk.getUnavailableReason() == null;
        StringBuilder lore = new StringBuilder(available ? "<a>" : "<8>").append(perk.getDisplayName());
        if (perk.getMaxTier() > 1) lore.append(" ").append(StringUtility.getAsRomanNumeral(shown));
        lore.append("\n").append(perk.description().replace("{value}", perk.valueLabel(shown))).append("\n\n");
        if (maxed) {
            lore.append("<a>Maxed out!");
        } else {
            lore.append("<7>Cost\n<d>").append(StringUtility.commaify(perk.costOfTier(shown))).append(" ")
                    .append(essence.getFullName()).append("\n\n");
            if (!available) {
                lore.append("<8>").append(perk.getUnavailableReason()).append("\n<c>Unavailable");
            } else if (Essences.get(player, essence) < perk.costOfTier(shown)) {
                lore.append("<c>You don't have enough ").append(essence.getFullName()).append("!");
            } else {
                lore.append(owned == 0 ? "<e>Click to unlock!" : "<e>Click to upgrade!");
            }
        }
        return ItemStacks.item(icon(perk), lore.toString());
    }

    private void buy(EssenceShop<CataloguePerk> shop, SkyBlockPlayer player, CataloguePerk perk) {
        switch (shop.buy(player, perk)) {
            case BOUGHT -> player.sendMessage("<a>You unlocked <e>{}<a>!", perk.getMaxTier() > 1
                    ? perk.getDisplayName() + " " + StringUtility.getAsRomanNumeral(EssencePerks.tier(player, perk))
                    : perk.getDisplayName());
            case UNAVAILABLE -> player.sendMessage("<c>{}", perk.getUnavailableReason());
            case TOO_POOR -> player.sendMessage("<c>You don't have enough {}!", essence.getFullName());
            case MAXED -> {}
        }
    }

    private static Material icon(CataloguePerk perk) {
        Material material = Material.fromKey(perk.icon().toLowerCase());
        return material == null ? Material.PAPER : material;
    }
}
