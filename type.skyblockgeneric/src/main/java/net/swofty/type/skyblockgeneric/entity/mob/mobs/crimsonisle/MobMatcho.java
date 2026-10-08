package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonSkins;

/**
 * Matcho (wiki: Matcho): a coal block flung by an eruption that stands still until attacked, then rushes the player.
 */
public class MobMatcho extends CrimsonIsleMob {
    public MobMatcho() {
        super(CrimsonMobs.MATCHO);
    }

    @Override
    protected Temper temper() {
        return Temper.NEUTRAL;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        CrimsonSkins.dress(this, CrimsonSkins.MATCHO);
    }
}
