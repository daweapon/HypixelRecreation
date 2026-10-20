package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.item.ItemType;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum StarlynTree {
    FIG("Fig", ItemType.FIG_LOG),
    MANGROVE("Mangrove", ItemType.MANGROVE_LOG),
    HELIX("Helix", ItemType.HELIX_LOG);

    private final String displayName;
    private final ItemType log;

    StarlynTree(String displayName, ItemType log) {
        this.displayName = displayName;
        this.log = log;
    }

    public static @Nullable StarlynTree ofLog(@Nullable ItemType type) {
        for (StarlynTree tree : values()) if (tree.log == type) return tree;
        return null;
    }

    public static @Nullable StarlynTree ofGift(String treeType) {
        for (StarlynTree tree : values()) if (tree.name().equalsIgnoreCase(treeType)) return tree;
        return null;
    }
}
