package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;
import net.swofty.type.skyblockgeneric.reforge.ReforgeHit;

public record SeaCreatureDefenseAbility(String reforgeId, double defense) implements ReforgeAbility.BonusDefense {
    @Override
    public double defenseAgainst(ReforgeHit.Incoming hit) {
        return hit.seaCreature() ? defense : 0;
    }
}
