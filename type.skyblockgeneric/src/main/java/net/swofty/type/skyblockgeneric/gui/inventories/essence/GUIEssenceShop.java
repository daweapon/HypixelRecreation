package net.swofty.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
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
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// The perk shop of any data driven essence (everything except Ice, Crimson and Fossil).
public class GUIEssenceShop extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

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
                <7>Exchange {}{} Essence <7>for perks!

                <7>Your {} Essence: <d>{}""", essence.getDisplayName(), entry.colour(), essence.getDisplayName(),
                essence.getDisplayName(), StringUtility.commaify(Essences.get((SkyBlockPlayer) c.player(), essence))));
        EssenceShop<CataloguePerk> shop = EssenceShops.shop(essence).orElseThrow();
        List<CataloguePerk> perks = shop.perks();
        for (int i = 0; i < perks.size() && i < SLOTS.length; i++) {
            CataloguePerk perk = perks.get(i);
            layout.slot(SLOTS[i], (_, c) -> perkItem((SkyBlockPlayer) c.player(), perk), (_, c) -> {
                buy(shop, (SkyBlockPlayer) c.player(), perk);
                c.session(DefaultState.class).refresh();
            });
        }
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
