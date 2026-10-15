package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.commons.skyblock.item.reforge.ReforgeType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;
import dev.daweapon.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

/**
 * Component for hoe tools.
 * Placeholder for future farming speed bonuses.
 */
public class HoeComponent extends SkyBlockItemComponent {
    public HoeComponent() {
        addInheritedComponent(new ExtraRarityComponent("HOE"));
        addInheritedComponent(new ReforgableComponent(ReforgeType.HOES));
        addInheritedComponent(new EnchantableComponent(List.of(EnchantItemGroups.TOOLS, EnchantItemGroups.HOE), true));
        addInheritedComponent(new TrackedUniqueComponent());
    }
}
