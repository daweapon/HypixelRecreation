package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderLeap;

public class MobDasherSpider extends SpiderDenMob {
    public MobDasherSpider(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected void onAiTick(int ticks) {
        SpiderLeap.DASHER.tick(this, ticks);
    }
}
