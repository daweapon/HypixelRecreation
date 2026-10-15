package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class MagneticAbility implements ReforgeAbility.MiningExperience {
    @Override
    public String reforgeId() {
        return "magnetic";
    }

    @Override
    public double bonusFraction() {
        return 1;
    }
}
