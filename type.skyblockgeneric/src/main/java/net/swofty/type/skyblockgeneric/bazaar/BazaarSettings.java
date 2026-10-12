package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Per-player Bazaar preferences: the bulk-instasell ignore list and the product view mode.
public final class BazaarSettings {
    private static final Map<UUID, Boolean> DIRECT_MODE = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<ItemType>> IGNORED = new ConcurrentHashMap<>();

    private BazaarSettings() {
    }

    public static boolean directMode(UUID player) {
        return DIRECT_MODE.getOrDefault(player, true);
    }

    public static boolean toggleDirectMode(UUID player) {
        boolean next = !directMode(player);
        DIRECT_MODE.put(player, next);
        return next;
    }

    public static Set<ItemType> ignored(UUID player) {
        return IGNORED.computeIfAbsent(player, key -> ConcurrentHashMap.newKeySet());
    }

    public static boolean isIgnored(UUID player, ItemType type) {
        return ignored(player).contains(type);
    }

    public static boolean toggleIgnored(UUID player, ItemType type) {
        Set<ItemType> ignored = ignored(player);
        if (ignored.remove(type)) {
            return false;
        }
        ignored.add(type);
        return true;
    }
}
