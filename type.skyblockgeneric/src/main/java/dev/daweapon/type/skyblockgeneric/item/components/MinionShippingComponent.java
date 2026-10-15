package dev.daweapon.type.skyblockgeneric.item.components;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class MinionShippingComponent extends SkyBlockItemComponent {
    @Getter
    private final double percentageOfOriginalPrice;

    public MinionShippingComponent(double percentageOfOriginalPrice) {
        this.percentageOfOriginalPrice = percentageOfOriginalPrice;
        addInheritedComponent(new TrackedUniqueComponent());
    }
}