package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

@Getter
public class QuiverDisplayComponent extends SkyBlockItemComponent {
    public boolean shouldBeArrow;

    public QuiverDisplayComponent(boolean shouldBeArrow) {
        this.shouldBeArrow = shouldBeArrow;
    }
}
