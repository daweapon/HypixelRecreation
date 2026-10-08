package net.swofty.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.essence.IceEssencePerk;
import net.swofty.type.skyblockgeneric.essence.IceEssenceShop;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

// Frozen Alex's Ice Essence Shop, laid out from the wiki's Frozen Alex/UI page. Drake Piper's icon
// isn't named on the wiki, so it uses a goat horn.
public class GUIIceEssenceShop extends StatelessView {
    private static final String WHITE_GIFT_TEXTURE = "10f5398510b1a05afc5b201ead8bfc583e57d7202f5193b0b761fcbd0ae2";
    private static final Map<IceEssencePerk, Integer> SLOTS = Map.of(
            IceEssencePerk.COLD_EFFICIENCY, 21,
            IceEssencePerk.COOLED_FORGES, 22,
            IceEssencePerk.FROZEN_SKIN, 23,
            IceEssencePerk.SEASON_OF_JOY, 30,
            IceEssencePerk.DRAKE_PIPER, 31);

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Ice Essence Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.slot(4, (_, c) -> header((SkyBlockPlayer) c.player()));
        SLOTS.forEach((perk, slot) -> layout.slot(slot, (_, c) -> perkItem((SkyBlockPlayer) c.player(), perk), (_, c) -> {
            buy((SkyBlockPlayer) c.player(), perk);
            c.session(DefaultState.class).refresh();
        }));
        layout.slot(32, (_, c) -> experienceBookItem(), (_, c) -> {
            buyExperienceBook((SkyBlockPlayer) c.player());
            c.session(DefaultState.class).refresh();
        });
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
    }

    private static ItemStack.Builder header(SkyBlockPlayer player) {
        return ItemStacks.head(EssenceGuideEntry.of(EssenceType.ICE).texture(), """
                <a>Ice Essence Shop
                <7>Exchange <d>Ice Essence <7>for
                <7>perks useful in the <c>Jerry's
                <c>Workshop <7>and beyond!

                <7>Your Ice Essence: <d>{}""", StringUtility.commaify(Essences.get(player, EssenceType.ICE)));
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, IceEssencePerk perk) {
        int owned = EssencePerks.tier(player, perk);
        boolean maxed = owned >= perk.getMaxTier();
        int shown = maxed ? owned : owned + 1;
        boolean available = perk.getUnavailableReason() == null;
        StringBuilder lore = new StringBuilder(available ? "<a>" : "<8>").append(perk.getDisplayName()).append(" ")
                .append(StringUtility.getAsRomanNumeral(shown)).append("\n")
                .append(description(perk, perk.valueAt(shown))).append("\n\n");
        if (maxed) {
            lore.append("<a>Maxed out!");
        } else {
            lore.append("<7>Cost\n<d>").append(StringUtility.commaify(perk.costOfTier(shown))).append(" Ice Essence\n\n");
            if (!available) {
                lore.append("<8>").append(perk.getUnavailableReason()).append("\n<c>Unavailable");
            } else if (Essences.get(player, EssenceType.ICE) < perk.costOfTier(shown)) {
                lore.append("<c>You don't have enough Ice Essence!");
            } else {
                lore.append(owned == 0 ? "<e>Click to unlock!" : "<e>Click to upgrade!");
            }
        }
        return switch (perk) {
            case COLD_EFFICIENCY -> ItemStacks.item(Material.CYAN_BANNER, lore.toString());
            case COOLED_FORGES -> ItemStacks.item(Material.FURNACE, lore.toString());
            case FROZEN_SKIN -> ItemStacks.item(Material.ICE, lore.toString());
            case SEASON_OF_JOY -> ItemStacks.head(WHITE_GIFT_TEXTURE, lore.toString());
            case DRAKE_PIPER -> ItemStacks.item(Material.GOAT_HORN, lore.toString());
        };
    }

    private static String description(IceEssencePerk perk, double value) {
        String amount = StringUtility.decimalify(value, 0);
        return switch (perk) {
            case COLD_EFFICIENCY -> "<7>Increases Mage class experience\n<7>gain by <a>" + amount + "%<7>.";
            case COOLED_FORGES -> "<7>Grants +<a>" + amount + "% <7>essence when salvaging.";
            case FROZEN_SKIN -> "<7>Gain <a>" + amount + " <b>❄ Cold Resistance<7>.";
            case SEASON_OF_JOY -> "<7>Gain <a>" + amount + "%<7> extra gifts from the\n<c>Gift Attack <7>event.";
            case DRAKE_PIPER -> "<7>Increases the chance to spawn a\n<6>Reindrake <7>by <a>" + amount + "% <7>while\n<7>fishing in <c>Jerry's Workshop<7>.";
        };
    }

    private static ItemStack.Builder experienceBookItem() {
        return ItemStacks.appendLore(new NonPlayerItemUpdater(experienceBook()).getUpdatedItem(), """

                <7>Cost
                <d>{} Ice Essence

                <e>Click to purchase!""", StringUtility.commaify(IceEssenceShop.EXPERIENCE_BOOK_COST));
    }

    private static SkyBlockItem experienceBook() {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(EnchantmentType.EXPERIENCE, 5));
        return book;
    }

    private static void buy(SkyBlockPlayer player, IceEssencePerk perk) {
        switch (IceEssenceShop.buy(player, perk)) {
            case BOUGHT -> player.sendMessage("<a>You unlocked <e>{} {}<a>!", perk.getDisplayName(),
                    StringUtility.getAsRomanNumeral(EssencePerks.tier(player, perk)));
            case UNAVAILABLE -> player.sendMessage("<c>{}", perk.getUnavailableReason());
            case TOO_POOR -> player.sendMessage("<c>You don't have enough Ice Essence!");
            case MAXED -> {}
        }
    }

    private static void buyExperienceBook(SkyBlockPlayer player) {
        if (!Essences.take(player, EssenceType.ICE, IceEssenceShop.EXPERIENCE_BOOK_COST)) {
            player.sendMessage("<c>You don't have enough Ice Essence!");
            return;
        }
        player.addAndUpdateItem(experienceBook());
        player.sendMessage("<a>You bought an <9>Experience V <a>book!");
    }
}
