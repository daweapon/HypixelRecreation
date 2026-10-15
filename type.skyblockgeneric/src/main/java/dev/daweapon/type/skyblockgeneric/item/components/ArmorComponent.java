package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class ArmorComponent extends SkyBlockItemComponent {
    public ArmorComponent() {
        addInheritedComponent(new TrackedUniqueComponent());
    }
}
