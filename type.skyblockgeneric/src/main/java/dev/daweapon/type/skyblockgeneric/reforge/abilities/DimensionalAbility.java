package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

public final class DimensionalAbility implements ReforgeAbility.TitaniumPowder {
    @Override
    public String reforgeId() {
        return "dimensional";
    }

    @Override
    public long bonusMithrilPowder() {
        return 10;
    }
}
