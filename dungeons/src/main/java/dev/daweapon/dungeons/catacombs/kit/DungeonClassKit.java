package dev.daweapon.dungeons.catacombs.kit;

import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;

import java.util.List;

public record DungeonClassKit(
        String id,
        String displayName,
        DungeonClassType type,
        List<DungeonKitItem> items
) {
    public DungeonClassKit {
        items = List.copyOf(items);
    }
}
