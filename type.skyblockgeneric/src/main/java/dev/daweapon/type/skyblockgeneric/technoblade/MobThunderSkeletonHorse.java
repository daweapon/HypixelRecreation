package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;

public class MobThunderSkeletonHorse extends CrimsonIsleMob {
    public MobThunderSkeletonHorse() {
        super(ThunderProfiles.SKELETON_HORSE);
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }
}
