package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

public class DrillComponent extends SkyBlockItemComponent {
    public DrillComponent() {
        addInheritedComponent(new EnchantableComponent(List.of(EnchantItemGroups.DRILL), true));
        addInheritedComponent(new ReforgableComponent(ReforgeType.PICKAXES));
        addInheritedComponent(new ExtraRarityComponent("DRILL"));
        addInheritedComponent(new TrackedUniqueComponent());
    }
}