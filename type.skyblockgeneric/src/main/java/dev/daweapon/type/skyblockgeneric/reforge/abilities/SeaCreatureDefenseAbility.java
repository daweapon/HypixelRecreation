package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeHit;

public record SeaCreatureDefenseAbility(String reforgeId, double defense) implements ReforgeAbility.BonusDefense {
    @Override
    public double defenseAgainst(ReforgeHit.Incoming hit) {
        return hit.seaCreature() ? defense : 0;
    }
}
