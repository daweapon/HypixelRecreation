package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.block.BlockType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import org.jetbrains.annotations.Nullable;

public class PlaceableComponent extends SkyBlockItemComponent {
    @Getter
    private final @Nullable BlockType blockType;

    public PlaceableComponent(@Nullable String blockType) {
        this.blockType = blockType != null ? BlockType.getFromName(blockType) : null;
    }

    public boolean isPlaceable() {
        return true;
    }
}