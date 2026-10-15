package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Bladesoul's Wither Guard (wiki: Bladesoul): a fast runner with 5M health and 4,000 damage.
 */
public class MobBladesoulWitherGuard extends CrimsonIsleMob {
    public MobBladesoulWitherGuard() {
        super(CrimsonMobs.WITHER_GUARD);
    }

    @Override
    protected float aggroRange() {
        return 24;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
