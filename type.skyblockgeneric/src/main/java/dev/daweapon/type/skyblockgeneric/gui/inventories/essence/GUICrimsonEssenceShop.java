package dev.daweapon.type.skyblockgeneric.gui.inventories.essence;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.essence.CrimsonEssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.CrimsonEssenceShop;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

// Vulcan's Crimson Essence Shop, laid out from the wiki's Vulcan/UI page.
public class GUICrimsonEssenceShop extends StatelessView {
    private static final Map<CrimsonEssencePerk, Integer> SLOTS = Map.of(
            CrimsonEssencePerk.STRONGARM, 20,
            CrimsonEssencePerk.FRESH_TOOLS, 21,
            CrimsonEssencePerk.FUNGUS_FORTUNA, 23,
            CrimsonEssencePerk.HARENA_FORTUNA, 24,
            CrimsonEssencePerk.HEADSTART, 29,
            CrimsonEssencePerk.KUUDRA_MASTER, 30,
            CrimsonEssencePerk.CRIMSON_TRAINING, 32,
            CrimsonEssencePerk.WITHER_PIPER, 33);

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crimson Essence Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        layout.slot(4, (_, c) -> header((SkyBlockPlayer) c.player()));
        SLOTS.forEach((perk, slot) -> layout.slot(slot, (_, c) -> perkItem((SkyBlockPlayer) c.player(), perk), (_, c) -> {
            buy((SkyBlockPlayer) c.player(), perk);
            c.session(DefaultState.class).refresh();
        }));
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
        return ItemStacks.head(EssenceGuideEntry.of(EssenceType.CRIMSON).texture(), """
                <a>Crimson Essence Shop
                <7>Exchange <d>Crimson Essence <7>for
                <7>perks useful on the <c>Crimson Isle
                <7>and in <c>Kuudra<7>!

                <7>Your Crimson Essence: <d>{}""", StringUtility.commaify(Essences.get(player, EssenceType.CRIMSON)));
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, CrimsonEssencePerk perk) {
        int owned = EssencePerks.tier(player, perk);
        boolean maxed = owned >= perk.getMaxTier();
        int shown = maxed ? owned : owned + 1;
        boolean available = perk.getUnavailableReason() == null;
        StringBuilder lore = new StringBuilder(available ? "<a>" : "<8>").append(perk.getDisplayName());
        if (perk.getMaxTier() > 1) lore.append(" ").append(StringUtility.getAsRomanNumeral(shown));
        lore.append("\n").append(description(perk, perk.valueAt(shown))).append("\n\n");
        if (maxed) {
            lore.append("<a>Maxed out!");
        } else {
            lore.append("<7>Cost\n<d>").append(StringUtility.commaify(perk.costOfTier(shown))).append(" Crimson Essence\n\n");
            if (!available) {
                lore.append("<c>").append(perk.getUnavailableReason()).append("\n<c>Unavailable");
            } else if (Essences.get(player, EssenceType.CRIMSON) < perk.costOfTier(shown)) {
                lore.append("<c>You don't have enough Crimson Essence!");
            } else {
                lore.append(owned == 0 ? "<e>Click to unlock!" : "<e>Click to upgrade!");
            }
        }
        return ItemStacks.item(icon(perk), lore.toString());
    }

    private static Material icon(CrimsonEssencePerk perk) {
        return switch (perk) {
            case STRONGARM -> Material.FISHING_ROD;
            case FRESH_TOOLS -> Material.WOODEN_AXE;
            case FUNGUS_FORTUNA -> Material.MYCELIUM;
            case HARENA_FORTUNA -> Material.RED_SAND;
            case HEADSTART -> Material.EMERALD;
            case KUUDRA_MASTER -> Material.ZOMBIE_HEAD;
            case CRIMSON_TRAINING -> Material.WRITABLE_BOOK;
            case WITHER_PIPER -> Material.NETHER_STAR;
        };
    }

    private static String description(CrimsonEssencePerk perk, double value) {
        String amount = StringUtility.decimalify(value, 1);
        return switch (perk) {
            case STRONGARM -> "<7>Fishing rods pull <a>Supplies <7>and\n<a>Fuel Cells <a>" + amount + "% <7>further in\n<c>Kuudra<7>.";
            case FRESH_TOOLS -> "<7>Grants a <a>" + amount + "% <7>chance to double\n<7>ballista repair speed for <a>5s <7>in\n<c>Kuudra<7>.";
            case FUNGUS_FORTUNA -> "<7>Grants <6>+" + amount + " ☘ Mining Fortune\n<7>when mining <a>Mycelium<7>.";
            case HARENA_FORTUNA -> "<7>Grants <6>+" + amount + " ☘ Mining Fortune\n<7>when mining <a>Red Sand<7>.";
            case HEADSTART -> "<7>Start <c>Kuudra <7>with <a>" + amount + " <7>Tokens.";
            case KUUDRA_MASTER -> "<7>Deal <a>+0.1% <7>damage to <c>Kuudra's\n<7>minions for each wave of your\n<7>best completed run in that tier\n<7>or higher. <8>(Max " + amount + "%)";
            case CRIMSON_TRAINING -> "<7>Grants <3>+" + amount + " ☯ Combat Wisdom <7>on the\n<c>Crimson Isle <7>and in <c>Kuudra<7>.";
            case WITHER_PIPER -> "<7>Grants <a>+" + amount + "% <7>chance to find\n<5>Vanquishers <7>on the <c>Crimson Isle<7>.";
        };
    }

    private static void buy(SkyBlockPlayer player, CrimsonEssencePerk perk) {
        switch (CrimsonEssenceShop.SHOP.buy(player, perk)) {
            case BOUGHT -> player.sendMessage("<a>You unlocked <e>{} {}<a>!", perk.getDisplayName(),
                    StringUtility.getAsRomanNumeral(EssencePerks.tier(player, perk)));
            case UNAVAILABLE -> player.sendMessage("<c>{}", perk.getUnavailableReason());
            case TOO_POOR -> player.sendMessage("<c>You don't have enough Crimson Essence!");
            case MAXED -> {
            }
        }
    }
}
