package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

public class DecorationHeadComponent extends SkyBlockItemComponent {
    public DecorationHeadComponent(String texture) {
        addInheritedComponent(new SkullHeadComponent((item) -> texture));
        addInheritedComponent(new PlaceableComponent("DECORATION"));
    }
}
