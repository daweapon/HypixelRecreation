package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;

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
