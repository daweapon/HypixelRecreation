package dev.daweapon.type.skyblockgeneric.dante;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;

public class MobDanteGoon extends CrimsonIsleMob {
    public MobDanteGoon() {
        super(DanteProfiles.GOON);
    }

    @Override
    protected float aggroRange() {
        return 80;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public void kill() {
        super.kill();
        DanteUprising.onGoonDown(this);
    }
}
