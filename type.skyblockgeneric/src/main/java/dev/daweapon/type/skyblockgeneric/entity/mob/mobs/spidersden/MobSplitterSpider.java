package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMobs;

public class MobSplitterSpider extends SpiderDenMob {
    private static final int SILVERFISH = 2;

    public MobSplitterSpider(CrimsonMobProfile profile) {
        super(profile);
    }

    @Override
    protected void afterDeath(Instance instance, Pos position) {
        for (int i = 0; i < SILVERFISH; i++) new SpiderDenMob(SpiderDenMobs.SILVERFISH).setInstance(instance, position);
    }
}
