package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class RoyalAbility implements ReforgeAbility.EventPowder {
    @Override
    public String reforgeId() {
        return "royal";
    }

    @Override
    public double eventPowderPercent() {
        return 15;
    }
}
