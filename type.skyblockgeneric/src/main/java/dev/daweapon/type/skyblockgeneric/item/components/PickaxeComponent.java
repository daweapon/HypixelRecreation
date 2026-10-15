package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

public class PickaxeComponent extends SkyBlockItemComponent {
    public PickaxeComponent() {
        addInheritedComponent(new EnchantableComponent(List.of(EnchantItemGroups.PICKAXE), true));
        addInheritedComponent(new ReforgableComponent(ReforgeType.PICKAXES));
        addInheritedComponent(new ExtraRarityComponent("PICKAXE"));
        addInheritedComponent(new TrackedUniqueComponent());
    }
}