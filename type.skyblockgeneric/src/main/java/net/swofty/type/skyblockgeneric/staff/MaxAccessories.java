package net.swofty.type.skyblockgeneric.staff;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.accessory.AccessoryBag;
import net.swofty.type.skyblockgeneric.accessory.AccessoryBagSlots;
import net.swofty.type.skyblockgeneric.accessory.AccessoryFamilies;
import net.swofty.type.skyblockgeneric.accessory.AccessoryFamily;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointAccessoryBag;
import net.swofty.type.skyblockgeneric.item.ConfigurableSkyBlockItem;
import net.swofty.type.skyblockgeneric.item.Recombobulation;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * {@code /maxprofile}'s accessory step: the best member of every family plus every standalone
 * accessory, recombobulated where allowed and with dynamic accessories at their top state, placed in
 * the Accessory Bag, which is then sorted highest rarity first. Rift-only and Bingo-only accessories and those
 * whose source content is not built yet (marked unavailable) are skipped. Driven by the item registry and the family data, so new accessories are covered without
 * touching this class. A rerun adds nothing it already placed.
 */
public final class MaxAccessories {
    private MaxAccessories() {
    }

    public static List<ItemType> plan() {
        Map<String, ItemType> accessories = new TreeMap<>();
        for (String id : ConfigurableSkyBlockItem.getIDs()) {
            ConfigurableSkyBlockItem config = ConfigurableSkyBlockItem.getFromID(id);
            ItemType type = ItemType.get(id);
            if (config == null || type == null || !config.hasComponent(AccessoryComponent.class)) continue;
            if (!config.getComponent(AccessoryComponent.class).getRealm().countsForPower()) continue;
            if (config.getUnavailable() != null) continue;
            accessories.put(id, type);
        }

        AccessoryFamilies families = AccessoryFamilies.get();
        Set<ItemType> plan = new LinkedHashSet<>();
        for (Map.Entry<String, ItemType> entry : accessories.entrySet()) {
            AccessoryFamily family = families.familyOf(entry.getKey());
            if (family == null) {
                plan.add(entry.getValue());
                continue;
            }
            ItemType best = bestAvailable(family, accessories);
            if (best != null) plan.add(best);
        }
        return List.copyOf(plan);
    }

    public static SkyBlockItem maxed(ItemType type) {
        SkyBlockItem item = new SkyBlockItem(type);
        maximise(item);
        return item;
    }

    public static void maximise(SkyBlockItem item) {
        AccessoryComponent definition = item.getComponent(AccessoryComponent.class);
        if (definition.getDynamicMaxRarity() != null) item.getAttributeHandler().setRarity(definition.getDynamicMaxRarity());
        if (Recombobulation.canRecombobulate(item)) item.getAttributeHandler().setRecombobulated(true);
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointAccessoryBag.PlayerAccessoryBag bag = player.getAccessoryBag();
        int left = fill(bag, AccessoryBagSlots.total(player));
        AccessoryBag.save(player, bag);
        if (left > 0) {
            Logger.warn("/maxprofile could not fit {} accessories into {}'s Accessory Bag", left, player.getUsername());
            player.sendMessage("<c>" + left + " accessories didn't fit in your Accessory Bag.");
        }
    }

    /** Places every planned accessory the bag lacks; returns how many didn't fit. */
    public static int fill(DatapointAccessoryBag.PlayerAccessoryBag bag, int totalSlots) {
        Set<ItemType> present = new LinkedHashSet<>();
        for (SkyBlockItem item : bag.getAllAccessories()) {
            ItemType type = item.getAttributeHandler().getPotentialType();
            if (type == null) continue;
            present.add(type);
            if (item.hasComponent(AccessoryComponent.class)) maximise(item);
        }

        List<ItemType> missing = new ArrayList<>(plan());
        missing.removeAll(present);
        int slot = 0;
        int left = 0;
        for (ItemType type : missing) {
            while (slot < totalSlots && bag.getInSlot(slot) != null) slot++;
            if (slot >= totalSlots) {
                left++;
                continue;
            }
            bag.setInSlot(slot, maxed(type));
        }
        sortByRarity(bag);
        return left;
    }

    public static void sortByRarity(DatapointAccessoryBag.PlayerAccessoryBag bag) {
        List<SkyBlockItem> sorted = new ArrayList<>(bag.getAllAccessories());
        sorted.sort(Comparator
                .comparing((SkyBlockItem item) -> item.getAttributeHandler().getEffectiveRarity()).reversed()
                .thenComparing(item -> String.valueOf(item.getAttributeHandler().getPotentialType())));
        bag.getAccessoryMap().clear();
        for (int slot = 0; slot < sorted.size(); slot++) bag.setInSlot(slot, sorted.get(slot));
    }

    private static @Nullable ItemType bestAvailable(AccessoryFamily family, Map<String, ItemType> accessories) {
        ItemType pick = accessories.get(family.best());
        if (pick != null) return pick;
        return family.members().stream()
                .map(accessories::get)
                .filter(type -> type != null)
                .max(Comparator.comparingInt((ItemType type) -> family.type() == AccessoryFamily.Type.UPGRADE_LINE
                        ? family.tierOf(type.name())
                        : maxed(type).getAttributeHandler().getEffectiveRarity().getAccessoryPower()))
                .orElse(null);
    }
}
