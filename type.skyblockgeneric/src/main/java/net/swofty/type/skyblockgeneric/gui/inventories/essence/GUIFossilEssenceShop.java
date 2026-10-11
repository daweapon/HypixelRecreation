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
import net.swofty.type.skyblockgeneric.essence.EssencePerks;
import net.swofty.type.skyblockgeneric.essence.EssenceType;
import net.swofty.type.skyblockgeneric.essence.Essences;
import net.swofty.type.skyblockgeneric.essence.FossilEssencePerk;
import net.swofty.type.skyblockgeneric.essence.FossilEssenceShop;
import net.swofty.type.skyblockgeneric.fossil.FossilService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

// Researcher Beryl's Fossil Essence Shop: six tiered perks and Suspicious Scrap.
public class GUIFossilEssenceShop extends StatelessView {
    private static final Map<FossilEssencePerk, Integer> SLOTS = Map.of(
            FossilEssencePerk.SLEIGHT_OF_HAND, 20,
            FossilEssencePerk.CHILLED_TO_THE_BONE, 21,
            FossilEssencePerk.DWARVEN_EXPERTISE, 22,
            FossilEssencePerk.CUT_LOOSE, 23,
            FossilEssencePerk.PREHISTORIAN, 24,
            FossilEssencePerk.RESOURCEFUL, 31);
    private static final int SCRAP_SLOT = 33;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fossil Essence Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
        layout.slot(4, (_, c) -> ItemStacks.item(Material.BONE_BLOCK, """
                <a>Fossil Essence Shop
                <7>Exchange <d>Fossil Essence <7>for perks
                <7>and items!

                <7>Your Fossil Essence: <d>{}""".replace("{}", StringUtility.commaify(Essences.get((SkyBlockPlayer) c.player(), EssenceType.FOSSIL)))));
        SLOTS.forEach((perk, slot) -> layout.slot(slot, (_, c) -> perkItem((SkyBlockPlayer) c.player(), perk), (_, c) -> {
            buy((SkyBlockPlayer) c.player(), perk);
            c.session(DefaultState.class).refresh();
        }));
        layout.slot(SCRAP_SLOT, (_, c) -> scrapItem((SkyBlockPlayer) c.player()), (_, c) -> {
            buyScrap((SkyBlockPlayer) c.player());
            c.session(DefaultState.class).refresh();
        });
    }

    private static ItemStack.Builder perkItem(SkyBlockPlayer player, FossilEssencePerk perk) {
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
            lore.append("<7>Cost\n<d>").append(StringUtility.commaify(perk.costOfTier(shown))).append(" Fossil Essence\n\n");
            if (!available) {
                lore.append("<8>").append(perk.getUnavailableReason()).append("\n<c>Unavailable");
            } else if (Essences.get(player, EssenceType.FOSSIL) < perk.costOfTier(shown)) {
                lore.append("<c>You don't have enough Fossil Essence!");
            } else {
                lore.append(owned == 0 ? "<e>Click to unlock!" : "<e>Click to upgrade!");
            }
        }
        return ItemStacks.item(icon(perk), lore.toString());
    }

    private static Material icon(FossilEssencePerk perk) {
        return switch (perk) {
            case SLEIGHT_OF_HAND -> Material.FEATHER;
            case CHILLED_TO_THE_BONE -> Material.BONE;
            case DWARVEN_EXPERTISE -> Material.IRON_PICKAXE;
            case CUT_LOOSE -> Material.SNOWBALL;
            case PREHISTORIAN -> Material.BROWN_DYE;
            case RESOURCEFUL -> Material.TRIPWIRE_HOOK;
        };
    }

    private static String description(FossilEssencePerk perk, double value) {
        String amount = StringUtility.decimalify(value, 1);
        return switch (perk) {
            case SLEIGHT_OF_HAND -> "<7>Gain <a>+" + (int) value + " <7>starting Chisel charge for the\n<6>Fossil Excavator<7>.";
            case CHILLED_TO_THE_BONE -> "<7>Increases your chance to fish up\n<b>Skeleton Fish <7>in the <c>Crimson Isle <7>by\n<a>" + amount + "%<7>.";
            case DWARVEN_EXPERTISE -> "<7>Gain <6>+" + amount + " ⸕ Dwarven Metal Fortune\n<7>on Mining Islands.";
            case CUT_LOOSE -> "<7>Each <b>Glacial <7>mob kill in a <b>Glacite\n<b>Mineshaft <7>grants <a>" + amount + " ❄ Cold Resistance<7>,\n<7>stacking up to 10 times.";
            case PREHISTORIAN -> "<7>Gain <a>+" + amount + "% <7>chance for <8>Fossil Dye\n<7>to spawn in the <6>Fossil Excavator<7>.";
            case RESOURCEFUL -> "<7>Gain a <a>" + amount + "% <7>chance to not consume\n<7>keys when looting a <b>Frozen Corpse<7>.";
        };
    }

    private static ItemStack.Builder scrapItem(SkyBlockPlayer player) {
        String affordable = Essences.get(player, EssenceType.FOSSIL) < FossilService.SCRAP_ESSENCE_COST
                ? "<c>You don't have enough Fossil Essence!" : "<e>Click to purchase!";
        return ItemStacks.appendLore(PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(ItemType.SUSPICIOUS_SCRAP).getItemStack()), """

                <7>Cost
                <d>{} Fossil Essence

                {}""", StringUtility.commaify(FossilService.SCRAP_ESSENCE_COST), affordable);
    }

    private static void buy(SkyBlockPlayer player, FossilEssencePerk perk) {
        switch (FossilEssenceShop.buy(player, perk)) {
            case BOUGHT -> player.sendMessage("<a>You unlocked <e>{} {}<a>!", perk.getDisplayName(),
                    StringUtility.getAsRomanNumeral(EssencePerks.tier(player, perk)));
            case UNAVAILABLE -> player.sendMessage("<c>{}", perk.getUnavailableReason());
            case TOO_POOR -> player.sendMessage("<c>You don't have enough Fossil Essence!");
            case MAXED -> {}
        }
    }

    private static void buyScrap(SkyBlockPlayer player) {
        if (!Essences.take(player, EssenceType.FOSSIL, FossilService.SCRAP_ESSENCE_COST)) {
            player.sendMessage("<c>You don't have enough Fossil Essence!");
            return;
        }
        player.addAndUpdateItem(new SkyBlockItem(ItemType.SUSPICIOUS_SCRAP));
        player.sendMessage("<a>You bought a <9>Suspicious Scrap<a>!");
    }
}
