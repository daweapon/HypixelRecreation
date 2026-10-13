package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;

public class MobBaldBlaze extends CrimsonIsleMob {

    public MobBaldBlaze() {
        super(CrystalHollowsBossProfiles.BALD_BLAZE);
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
