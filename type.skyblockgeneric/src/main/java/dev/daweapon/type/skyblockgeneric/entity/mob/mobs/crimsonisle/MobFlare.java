package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonBlazeMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Flare (wiki: Flare): a stationary sentry on the Magma Chamber pillars with poor range and no knockback.
 */
public class MobFlare extends CrimsonBlazeMob {
    public MobFlare() {
        super(CrimsonMobs.FLARE);
    }

    @Override
    protected boolean flies() {
        return false;
    }

    @Override
    protected double attackRange() {
        return 12;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
    }

    @Override
    public void takeKnockback(float strength, double x, double z) {
    }
}
