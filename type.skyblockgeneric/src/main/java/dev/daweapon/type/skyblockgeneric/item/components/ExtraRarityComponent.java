package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.function.Function;

// TODO: take a Component + make a deserializer for minimessage
@Getter
public class ExtraRarityComponent extends SkyBlockItemComponent {
    private final Function<SkyBlockItem, String> extraDisplay;

    public ExtraRarityComponent(String extraDisplay) {
        this(_ -> extraDisplay);
    }

    public ExtraRarityComponent(Function<SkyBlockItem, String> extraDisplay) {
        this.extraDisplay = extraDisplay;
    }

    public String getExtraRarityDisplay(SkyBlockItem item) {
        return extraDisplay.apply(item);
    }
}
