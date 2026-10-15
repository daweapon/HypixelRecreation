package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class MarshyAbility implements ReforgeAbility.ForestWhispers {
    @Override
    public String reforgeId() {
        return "marshy";
    }

    @Override
    public double bonusFraction() {
        return 0.01;
    }
}
