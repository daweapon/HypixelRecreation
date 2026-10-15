package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.function.Function;

public class SkullHeadComponent extends SkyBlockItemComponent {
    private final Function<SkyBlockItem, String> textureProvider;

    public SkullHeadComponent(Function<SkyBlockItem, String> textureProvider) {
        this.textureProvider = textureProvider;
    }

    public String getSkullTexture(SkyBlockItem item) {
        return textureProvider.apply(item);
    }
}