package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class CandiedAbility implements ReforgeAbility.CandyDrop {
    @Override
    public String reforgeId() {
        return "candied";
    }

    @Override
    public double dropChanceBonus() {
        return 0.01;
    }
}
