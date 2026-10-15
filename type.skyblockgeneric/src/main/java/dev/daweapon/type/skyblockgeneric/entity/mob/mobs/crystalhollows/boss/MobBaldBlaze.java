package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;

public class MobBaldBlaze extends CrimsonIsleMob {

    public MobBaldBlaze() {
        super(CrystalHollowsBossProfiles.BALD_BLAZE);
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
