package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

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
