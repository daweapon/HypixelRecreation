package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

@Getter
public class ReforgableComponent extends SkyBlockItemComponent {
    private final ReforgeType reforgeType;

    public ReforgableComponent(ReforgeType reforgeType) {
        this.reforgeType = reforgeType;
    }
}