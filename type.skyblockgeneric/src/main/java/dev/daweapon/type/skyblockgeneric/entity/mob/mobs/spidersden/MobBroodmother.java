package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMobs;

import java.util.concurrent.ThreadLocalRandom;

public class MobBroodmother extends SpiderDenMob {
    private static final double HIT_CAP = 300;

    public MobBroodmother() {
        super(SpiderDenMobs.BROODMOTHER);
    }

    @Override
    protected double hitCap() {
        return HIT_CAP;
    }

    @Override
    protected void afterDeath(Instance instance, Pos position) {
        int spawn = ThreadLocalRandom.current().nextInt(4, 6);
        for (int i = 0; i < spawn; i++) new SpiderDenMob(SpiderDenMobs.BROOD_CAVE_SPIDER).setInstance(instance, position);
    }
}
