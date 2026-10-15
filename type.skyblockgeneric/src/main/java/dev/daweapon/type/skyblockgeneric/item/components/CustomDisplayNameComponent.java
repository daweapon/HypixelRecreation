package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.function.Function;

public class CustomDisplayNameComponent extends SkyBlockItemComponent {
    private final Function<SkyBlockItem, Text> displayNameProvider;

    public CustomDisplayNameComponent(Function<SkyBlockItem, Text> displayNameProvider) {
        this.displayNameProvider = displayNameProvider;
    }

    public Text getDisplayName(SkyBlockItem item) {
        return displayNameProvider.apply(item);
    }
}
