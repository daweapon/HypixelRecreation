package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;
import net.swofty.type.skyblockgeneric.reforge.ReforgeHit;

public final class FabledAbility implements ReforgeAbility.OutgoingDamage {
    public static final double MAX_BONUS = 0.15;

    @Override
    public String reforgeId() {
        return "fabled";
    }

    @Override
    public double outgoingMultiplier(ReforgeHit.Outgoing hit) {
        return hit.critical() ? 1 + hit.random().nextDouble() * MAX_BONUS : 1;
    }
}
