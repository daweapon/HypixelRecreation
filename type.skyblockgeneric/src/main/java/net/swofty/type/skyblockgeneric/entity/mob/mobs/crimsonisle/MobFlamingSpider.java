package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;

public class MobFlamingSpider extends CrimsonIsleMob {
    public MobFlamingSpider() {
        super(CrimsonMobs.FLAMING_SPIDER);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setFireTicks(Integer.MAX_VALUE);
    }
}
