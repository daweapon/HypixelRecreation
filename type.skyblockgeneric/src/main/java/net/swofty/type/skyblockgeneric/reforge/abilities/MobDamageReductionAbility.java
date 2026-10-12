package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;
import net.swofty.type.skyblockgeneric.reforge.ReforgeHit;

public record MobDamageReductionAbility(String reforgeId, MobType type, double percent)
        implements ReforgeAbility.IncomingDamage {
    @Override
    public double incomingMultiplier(ReforgeHit.Incoming hit) {
        return hit.mobTypes().contains(type) ? 1 - percent / 100D : 1;
    }
}
