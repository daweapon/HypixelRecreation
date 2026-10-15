package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.hunting.AttributeDefinition;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class StickyAbility implements ReforgeAbility.HunterFortune {
    @Override
    public String reforgeId() {
        return "sticky";
    }

    @Override
    public double fortuneFor(AttributeDefinition.AttributeCategory category) {
        return category == AttributeDefinition.AttributeCategory.WATER ? 2.5 : 0;
    }
}
