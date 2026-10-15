package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class EruditeAbility implements ReforgeAbility.BurrowChainLength {
    public static final int EXTRA_BURROWS = 2;

    @Override
    public String reforgeId() {
        return "erudite";
    }

    @Override
    public int extraBurrows() {
        return EXTRA_BURROWS;
    }
}
