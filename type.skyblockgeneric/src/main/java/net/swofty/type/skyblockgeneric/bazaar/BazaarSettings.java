package net.swofty.type.skyblockgeneric.bazaar;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.data.datapoints.DatapointStringList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Per-player Bazaar preferences: the bulk-instasell ignore list and the product view mode.
public final class BazaarSettings {
    private BazaarSettings() {
    }

    public static boolean directMode(UUID player) {
        return handler(player)
                .map(handler -> handler.get(HypixelDataHandler.Data.BAZAAR_DIRECT_MODE, DatapointBoolean.class).getValue())
                .map(Boolean.TRUE::equals)
                .orElse(true);
    }

    public static boolean toggleDirectMode(UUID player) {
        boolean next = !directMode(player);
        handler(player).ifPresent(handler ->
                handler.get(HypixelDataHandler.Data.BAZAAR_DIRECT_MODE, DatapointBoolean.class).setValue(next));
        return next;
    }

    public static Set<ItemType> ignored(UUID player) {
        return handler(player)
                .map(handler -> decode(handler.get(HypixelDataHandler.Data.BAZAAR_IGNORED_ITEMS, DatapointStringList.class).getValue()))
                .orElseGet(LinkedHashSet::new);
    }

    public static boolean isIgnored(UUID player, ItemType type) {
        return ignored(player).contains(type);
    }

    public static boolean toggleIgnored(UUID player, ItemType type) {
        Set<ItemType> ignored = ignored(player);
        boolean nowIgnored = !ignored.remove(type);
        if (nowIgnored) {
            ignored.add(type);
        }
        handler(player).ifPresent(handler ->
                handler.get(HypixelDataHandler.Data.BAZAAR_IGNORED_ITEMS, DatapointStringList.class).setValue(encode(ignored)));
        return nowIgnored;
    }

    static List<String> encode(Collection<ItemType> types) {
        return new ArrayList<>(types.stream().map(ItemType::name).toList());
    }

    static Set<ItemType> decode(Collection<String> names) {
        Set<ItemType> types = new LinkedHashSet<>();
        for (String name : names) {
            ItemType type = name.isEmpty() ? null : ItemType.get(name);
            if (type != null) {
                types.add(type);
            }
        }
        return types;
    }

    private static Optional<HypixelDataHandler> handler(UUID player) {
        return HypixelDataHandler.findHypixelUser(player);
    }
}
