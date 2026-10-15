package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonBlazeMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Elle's Blazes (wiki: Blaze_(Elle's_Quest)): stationary Blazes at the Stronghold entrance that start Elle's
 * questline. The arrival questline spawns them per player; they never respawn.
 */
public class MobIntroBlaze extends CrimsonBlazeMob {
    public MobIntroBlaze() {
        super(CrimsonMobs.INTRO_BLAZE);
    }

    @Override
    protected boolean flies() {
        return false;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
