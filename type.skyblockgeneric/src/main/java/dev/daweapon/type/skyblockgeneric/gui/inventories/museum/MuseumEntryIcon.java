package dev.daweapon.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.museum.MuseumEntry;
import dev.daweapon.type.skyblockgeneric.museum.MuseumService;
import dev.daweapon.type.skyblockgeneric.museum.MuseumState;
import dev.daweapon.type.skyblockgeneric.utility.ItemPriceCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class MuseumEntryIcon {
    private static final Text SEPARATOR = Text.of("<8><m>---------------------");

    private MuseumEntryIcon() {}

    static ItemStack.Builder render(DatapointMuseum.MuseumData data, MuseumEntry entry, boolean extraDetails) {
        return switch (MuseumService.stateOf(data, entry)) {
            case IN_MUSEUM -> inMuseum(data, entry, extraDetails);
            case RETRIEVED -> ItemStacks.item(Material.LIME_DYE, 1, Text.of("<a>{}", entry.name()), List.of(
                    Text.of("<7>You have retrieved this from your"),
                    Text.of("<7>Museum but can add it back at any"),
                    Text.of("<7>time."),
                    Text.empty(),
                    Text.of("<e>Click it in your inventory to return it!")));
            case CREDITED -> credited(data, entry);
            case MISSING -> ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>{}", entry.name()), missingLore(entry));
            case UNAVAILABLE -> ItemStacks.item(Material.LIGHT_GRAY_DYE, 1, Text.of("<8>{}", entry.name()),
                    withReward(entry, List.of(Text.of(MuseumEntry.UNAVAILABLE_REASON))));
        };
    }

    private static ItemStack.Builder inMuseum(DatapointMuseum.MuseumData data, MuseumEntry entry, boolean extraDetails) {
        List<SkyBlockItem> items = data.itemsFor(entry, true);
        SkyBlockItem shown = items.getFirst();
        List<Text> lore = new ArrayList<>();
        lore.add(SEPARATOR);
        if (extraDetails) lore.addAll(details(data, items));
        lore.add(Text.of(entry.isSet() ? "<e>Click to retrieve set!" : "<e>Click to retrieve item!"));

        ItemStack.Builder builder = new NonPlayerItemUpdater(shown).getUpdatedItem();
        if (entry.isSet()) builder = ItemStacks.name(builder, Text.of("<color:{}>{}",
                shown.getAttributeHandler().getRarity().getColor(), entry.name()));
        return ItemStacks.appendLore(builder, lore);
    }

    private static List<Text> details(DatapointMuseum.MuseumData data, List<SkyBlockItem> items) {
        List<Text> lore = new ArrayList<>();
        UUID first = UUID.fromString(items.getFirst().getAttributeHandler().getUniqueTrackedID());
        Long donated = data.getInsertionTimes().get(first);
        lore.add(Text.of("<7>Item Donated"));
        lore.add(Text.of("<b>{}", donated == null ? "Unknown" : StringUtility.formatAsDate(donated)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Item Clean Value"));
        double clean = items.stream().mapToDouble(item -> new ItemPriceCalculator(item).calculateCleanPrice()).sum();
        lore.add(Text.of("<6>{} Coins", StringUtility.commaify(clean)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Item Value"));
        if (data.isHasBoughtAppraisalService()) {
            double value = items.stream()
                    .mapToDouble(item -> data.getCalculatedPrices().getOrDefault(
                            UUID.fromString(item.getAttributeHandler().getUniqueTrackedID()), 0D))
                    .sum();
            lore.add(Text.of("<6>{} Coins", StringUtility.commaify(value)));
        } else {
            lore.add(Text.of("<c>Requires the Appraisal Service"));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<7>Museum Display Slot"));
        DatapointMuseum.DisplayPlacement placement = data.getDisplayHandler().getItemDisplayPlacement(items.getFirst());
        if (placement == null) {
            lore.add(Text.of("<c>Not In Display"));
        } else {
            lore.add(Text.of("<9>{} Slot #{}", placement.display(), placement.slot() + 1));
        }
        lore.add(Text.empty());
        return lore;
    }

    private static ItemStack.Builder credited(DatapointMuseum.MuseumData data, MuseumEntry entry) {
        ItemType icon = entry.iconType();
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<a>Donated!"));
        MuseumService.catalogue().upgradeOf(entry)
                .filter(upgrade -> data.isCredited(upgrade.id()))
                .ifPresent(upgrade -> lore.add(Text.of("<7>Counted from your <f>{}<7>.", upgrade.name())));
        lore.add(Text.empty());
        lore.add(Text.of("<7>No item is stored in your Museum for"));
        lore.add(Text.of("<7>this entry. Click a matching item in"));
        lore.add(Text.of("<7>your inventory to display it here."));
        return ItemStacks.item(icon == null ? Material.LIME_DYE : icon.material, 1, Text.of("<a>{}", entry.name()), lore);
    }

    private static List<Text> missingLore(MuseumEntry entry) {
        List<Text> lore = new ArrayList<>();
        if (entry.isSet()) {
            lore.add(Text.of("<7>Click on an armor piece in your"));
            lore.add(Text.of("<7>inventory that belongs to this armor"));
            lore.add(Text.of("<7>set to donate the full set to your"));
            lore.add(Text.of("<9>Museum<7>."));
        } else {
            lore.add(Text.of("<7>Click on this item in your inventory to"));
            lore.add(Text.of("<7>add it to your <9>Museum<7>!"));
        }
        MuseumService.catalogue().upgradeOf(entry).ifPresent(upgrade -> {
            lore.add(Text.empty());
            if (entry.isSet()) {
                lore.add(Text.of("<7>Donating <f>{} <7>will give", upgrade.name()));
                lore.add(Text.of("<7>credit for logging this armor set."));
            } else {
                lore.add(Text.of("<7>Donating a <f>{} <7>will count", upgrade.name()));
                lore.add(Text.of("<7>as donating this item."));
            }
        });
        return withReward(entry, lore);
    }

    private static List<Text> withReward(MuseumEntry entry, List<Text> lines) {
        List<Text> lore = new ArrayList<>(lines);
        if (entry.category().isSpecial()) return lore;
        lore.add(Text.empty());
        lore.add(Text.of("<7>Reward: <b>+{} SkyBlock XP", entry.xp()));
        return lore;
    }
}
