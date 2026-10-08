package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Mushroom Bull (wiki: Mushroom_Bull): a fast charging bull in the Mystic Marsh.
 */
public class MobMushroomBull extends CrimsonIsleMob {
    public MobMushroomBull() {
        super(CrimsonMobs.MUSHROOM_BULL);
    }

    @Override
    protected int strollRadius() {
        return 12;
    }
}
