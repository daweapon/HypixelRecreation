package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Ghast (wiki: Ghast): floats over the Wasteland at night and shoots fireballs.
 */
public class MobGhast extends CrimsonIsleMob {
    public MobGhast() {
        super(CrimsonMobs.GHAST);
    }

    private static final int FIREBALL_INTERVAL_TICKS = 60;

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % FIREBALL_INTERVAL_TICKS != 0) return;
        rangedTarget(40).ifPresent(player -> fireball(player, getProfile().damage()));
    }
}
