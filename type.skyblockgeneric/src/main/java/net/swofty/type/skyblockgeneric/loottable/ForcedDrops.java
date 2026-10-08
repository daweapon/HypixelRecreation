package net.swofty.type.skyblockgeneric.loottable;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Staff-forced drops: the next mob a player kills whose drop table contains a forced item always
 * drops it, once.
 */
public final class ForcedDrops {
    private static final Map<UUID, Set<ItemType>> FORCED = new ConcurrentHashMap<>();

    private ForcedDrops() {
    }

    public static void force(UUID player, ItemType item) {
        FORCED.computeIfAbsent(player, ignored -> ConcurrentHashMap.newKeySet()).add(item);
    }

    public static boolean consume(UUID player, ItemType item) {
        Set<ItemType> forced = FORCED.get(player);
        return forced != null && forced.remove(item);
    }

    public static Set<ItemType> pending(UUID player) {
        return Set.copyOf(FORCED.getOrDefault(player, Set.of()));
    }
}
