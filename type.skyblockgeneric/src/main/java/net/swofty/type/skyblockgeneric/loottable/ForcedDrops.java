package net.swofty.type.skyblockgeneric.loottable;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Staff-forced drops: the next chance-based drop that could give a player a forced item always
 * does, once. {@link LootRoll} consults this for every RNG drop. A loaded player's forced drops are
 * saved on their profile so they follow them between servers.
 */
public final class ForcedDrops {
    private static final Map<UUID, Set<ItemType>> UNLOADED = new ConcurrentHashMap<>();

    private ForcedDrops() {
    }

    public static void force(UUID player, ItemType item) {
        DatapointStringList saved = saved(player);
        if (saved == null) {
            UNLOADED.computeIfAbsent(player, ignored -> ConcurrentHashMap.newKeySet()).add(item);
            return;
        }
        if (saved.getValue().contains(item.name())) return;
        List<String> forced = new ArrayList<>(saved.getValue());
        forced.add(item.name());
        saved.setValue(forced);
    }

    public static boolean consume(UUID player, ItemType item) {
        Set<ItemType> unloaded = UNLOADED.get(player);
        if (unloaded != null && unloaded.remove(item)) return true;
        DatapointStringList saved = saved(player);
        if (saved == null || !saved.getValue().contains(item.name())) return false;
        List<String> forced = new ArrayList<>(saved.getValue());
        forced.remove(item.name());
        saved.setValue(forced);
        return true;
    }

    public static boolean isForced(UUID player, Collection<ItemType> items) {
        Set<ItemType> forced = pending(player);
        return items.stream().anyMatch(forced::contains);
    }

    public static Set<ItemType> pending(UUID player) {
        Set<ItemType> forced = new HashSet<>(UNLOADED.getOrDefault(player, Set.of()));
        DatapointStringList saved = saved(player);
        if (saved != null) {
            saved.getValue().stream().map(ItemType::get).filter(type -> type != null).forEach(forced::add);
        }
        return forced;
    }

    private static @Nullable DatapointStringList saved(UUID player) {
        SkyBlockPlayer online = SkyBlockGenericLoader.getFromUUID(player);
        if (online == null || online.getSkyblockDataHandler() == null) return null;
        return online.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FORCED_DROPS, DatapointStringList.class);
    }
}
