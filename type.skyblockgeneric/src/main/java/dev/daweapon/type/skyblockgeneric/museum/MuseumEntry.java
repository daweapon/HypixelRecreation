package dev.daweapon.type.skyblockgeneric.museum;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record MuseumEntry(
        String id,
        String name,
        MuseumCategory category,
        int xp,
        @Nullable String stage,
        @Nullable String parent,
        List<String> mapped,
        List<String> pieces
) {
    public static final String UNAVAILABLE_REASON = "<c>This item is not in the game yet!";

    public boolean isSet() {
        return !pieces.isEmpty();
    }

    public List<String> requiredItemIds() {
        return isSet() ? pieces : List.of(id);
    }

    public List<ItemType> requiredItemTypes() {
        return requiredItemIds().stream().map(ItemType::get).filter(Objects::nonNull).toList();
    }

    public List<ItemType> grantableItemTypes() {
        return Stream.concat(requiredItemIds().stream(), isSet() ? Stream.<String>empty() : mapped.stream())
                .map(ItemType::get).filter(Objects::nonNull).distinct().toList();
    }

    public boolean isAvailable() {
        return requiredItemIds().stream().allMatch(itemId -> ItemType.get(itemId) != null);
    }

    public boolean accepts(ItemType type) {
        String name = type.name();
        if (isSet()) return pieces.contains(name);
        return id.equals(name) || mapped.contains(name);
    }

    public @Nullable ItemType iconType() {
        return ItemType.get(requiredItemIds().getFirst());
    }
}
