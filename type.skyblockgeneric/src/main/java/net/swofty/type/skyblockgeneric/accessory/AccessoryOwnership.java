package net.swofty.type.skyblockgeneric.accessory;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The one rule for which accessories a player has working, used by the bag, stats, Accessory Power
 * and every accessory effect. Accessories count from the inventory or the Accessory Bag; duplicates
 * never stack; within a family only the best member counts (the highest tier of an upgrade line, the
 * strongest member of an "only one counts" group). Only the counted ones that sit in the bag add
 * Accessory Power.
 */
public final class AccessoryOwnership {
    private static final long CACHE_MILLIS = 1000;
    private static final Map<SkyBlockPlayer, Snapshot> CACHE = new WeakHashMap<>();

    private record Snapshot(List<OwnedAccessory> active, long createdAt) {
    }

    private AccessoryOwnership() {
    }

    public static List<OwnedAccessory> active(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        synchronized (CACHE) {
            Snapshot snapshot = CACHE.get(player);
            if (snapshot != null && now - snapshot.createdAt() < CACHE_MILLIS) return snapshot.active();
        }
        List<OwnedAccessory> active = dedupe(collect(player));
        synchronized (CACHE) {
            CACHE.put(player, new Snapshot(active, now));
        }
        return active;
    }

    public static void invalidate(SkyBlockPlayer player) {
        synchronized (CACHE) {
            CACHE.remove(player);
        }
    }

    public static boolean owns(SkyBlockPlayer player, ItemType type) {
        return active(player).stream().anyMatch(owned -> owned.type() == type);
    }

    public static List<ItemType> ownedTypes(SkyBlockPlayer player) {
        return active(player).stream().map(OwnedAccessory::type).toList();
    }

    public static List<SkyBlockItem> activeItems(SkyBlockPlayer player) {
        return active(player).stream().map(OwnedAccessory::item).toList();
    }

    public static List<OwnedAccessory> collect(SkyBlockPlayer player) {
        List<OwnedAccessory> candidates = new ArrayList<>();
        for (SkyBlockItem item : player.getAccessoryBag().getAllAccessories()) add(candidates, item, true);
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            add(candidates, new SkyBlockItem(stack), false);
        }
        return candidates;
    }

    /** Keeps one accessory per family (or per item id when it has none), preferring bag copies on ties. */
    public static List<OwnedAccessory> dedupe(List<OwnedAccessory> candidates) {
        AccessoryFamilies families = AccessoryFamilies.get();
        Map<String, OwnedAccessory> best = new LinkedHashMap<>();
        for (OwnedAccessory candidate : candidates) {
            AccessoryFamily family = families.familyOf(candidate.type().name());
            String key = family == null ? "item:" + candidate.type().name() : "family:" + family.id();
            best.merge(key, candidate, (current, next) -> ranking(family).compare(next, current) > 0 ? next : current);
        }
        return List.copyOf(best.values());
    }

    private static Comparator<OwnedAccessory> ranking(AccessoryFamily family) {
        Comparator<OwnedAccessory> byTier = Comparator.comparingInt(owned -> family == null ? 0 : family.tierOf(owned.type().name()));
        Comparator<OwnedAccessory> byPower = Comparator.comparingInt(OwnedAccessory::rarityPower);
        Comparator<OwnedAccessory> byBag = Comparator.comparing(OwnedAccessory::inBag);
        Comparator<OwnedAccessory> primary = family != null && family.type() == AccessoryFamily.Type.UPGRADE_LINE
                ? byTier.thenComparing(byPower)
                : byPower.thenComparing(byTier);
        return primary.thenComparing(byBag);
    }

    private static void add(List<OwnedAccessory> candidates, SkyBlockItem item, boolean inBag) {
        OwnedAccessory owned = OwnedAccessory.of(item, inBag);
        if (owned != null) candidates.add(owned);
    }
}
