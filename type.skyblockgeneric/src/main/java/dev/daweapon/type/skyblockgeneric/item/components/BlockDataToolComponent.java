package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class BlockDataToolComponent extends SkyBlockItemComponent {

    public BlockDataToolComponent() {
        addInheritedComponent(new TrackedUniqueComponent());
    }

}
