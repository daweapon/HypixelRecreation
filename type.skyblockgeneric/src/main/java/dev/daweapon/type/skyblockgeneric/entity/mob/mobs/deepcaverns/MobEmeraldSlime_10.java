package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;

public class MobEmeraldSlime_10 extends MobEmeraldSlime_05 {
    @Override
    public Integer getLevel() {
        return 10;
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 150D)
                .withBase(ItemStatistic.DAMAGE, 100D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(15, 8, 30);
    }

    @Override
    public String getMobID() {
        return "EMERALD_SLIME_10";
    }
}
