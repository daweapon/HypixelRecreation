package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows.boss;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;

public class MobFireBat extends CrimsonIsleMob {

    public MobFireBat() {
        super(CrystalHollowsBossProfiles.FIRE_BAT);
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
