package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobHoldoutKnifethrower extends MobHoldoutGoblin {
    @Override
    public Integer getLevel() {
        return 23;
    }

    @Override
    public String getMobID() {
        return "GOBLIN_HOLDOUT_KNIFETHROWER";
    }

    @Override
    public String getDisplayName() {
        return "Knifethrower";
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 800D)
                .withBase(ItemStatistic.DAMAGE, 300D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.GOBLIN_HOLDOUT, 3));
    }
}
