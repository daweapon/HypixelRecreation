package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeHit;

public record MobDamageReductionAbility(String reforgeId, MobType type, double percent)
        implements ReforgeAbility.IncomingDamage {
    @Override
    public double incomingMultiplier(ReforgeHit.Incoming hit) {
        return hit.mobTypes().contains(type) ? 1 - percent / 100D : 1;
    }
}
