package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class StiffAbility implements ReforgeAbility.SeaCreatureChum {
    @Override
    public String reforgeId() {
        return "stiff";
    }

    @Override
    public int extraChum() {
        return 1;
    }
}
