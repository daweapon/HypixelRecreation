package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;

public class MobMinerZombie_20 extends MobMinerZombie_15 {
    @Override
    public Integer getLevel() {
        return 15;
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 300D)
                .withBase(ItemStatistic.DAMAGE, 250D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(24, 15, 40);
    }

    @Override
    public String getMobID() {
        return "DIAMOND_ZOMBIE_20";
    }
}
