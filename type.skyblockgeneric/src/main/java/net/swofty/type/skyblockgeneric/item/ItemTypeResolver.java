package net.swofty.type.skyblockgeneric.item;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ItemTypeResolver {
    private static volatile Map<String, ItemType> byName;

    private ItemTypeResolver() {}

    public static @Nullable ItemType resolve(String input) {
        ItemType exact = ItemType.get(normalize(input));
        if (exact != null) return exact;
        Map<String, ItemType> names = byName;
        if (names == null) {
            names = index(ConfigurableSkyBlockItem::getConfiguredName);
            byName = names;
        }
        return names.get(compact(input));
    }

    static @Nullable ItemType resolve(String input, Function<String, String> configuredName) {
        ItemType exact = ItemType.get(normalize(input));
        return exact != null ? exact : index(configuredName).get(compact(input));
    }

    private static Map<String, ItemType> index(Function<String, String> configuredName) {
        Map<String, ItemType> names = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            String name = configuredName.apply(type.name());
            if (name != null) names.putIfAbsent(compact(name), type);
        }
        for (ItemType type : ItemType.values()) {
            names.putIfAbsent(compact(type.name()), type);
        }
        return names;
    }

    private static String normalize(String input) {
        return input.trim().toUpperCase().replace(' ', '_').replace('-', '_');
    }

    private static String compact(String value) {
        return value.replaceAll("<[^>]*>", "").toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}
