package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;

public class MobSpiderJockey extends SpiderDenMob {
    private final CrimsonMobProfile riderProfile;

    public MobSpiderJockey(CrimsonMobProfile profile, CrimsonMobProfile riderProfile) {
        super(profile);
        this.riderProfile = riderProfile;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        MobJockeySkeleton rider = new MobJockeySkeleton(riderProfile);
        rider.setInstance(getInstance(), getPosition());
        addPassenger(rider);
    }
}
