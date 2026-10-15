package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

public class ShovelComponent extends SkyBlockItemComponent {
    public ShovelComponent() {
        addInheritedComponent(new ExtraRarityComponent("SHOVEL"));
        addInheritedComponent(new EnchantableComponent(List.of(EnchantItemGroups.TOOLS, EnchantItemGroups.SHOVEL), true));
        addInheritedComponent(new TrackedUniqueComponent());
    }
}