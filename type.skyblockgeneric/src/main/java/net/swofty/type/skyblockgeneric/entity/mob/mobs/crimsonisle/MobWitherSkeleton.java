package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Wither Skeleton (wiki: Wither_Skeleton): sprints at players up to 32 blocks away; each hit withers for 10 s.
 */
public class MobWitherSkeleton extends CrimsonIsleMob {
    public MobWitherSkeleton() {
        super(CrimsonMobs.WITHER_SKELETON);
    }

    @Override
    protected float aggroRange() {
        return 32;
    }

    @Override
    public void onMeleeHit(SkyBlockPlayer target) {
        target.addEffect(new Potion(PotionEffect.WITHER, (byte) 0, 200));
    }
}
