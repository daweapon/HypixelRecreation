package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobHoldoutMurderlover extends MobHoldoutGoblin {
    @Override
    public Integer getLevel() {
        return 47;
    }

    @Override
    public String getMobID() {
        return "GOBLIN_HOLDOUT_MURDERLOVER";
    }

    @Override
    public String getDisplayName() {
        return "Murderlover";
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 50_000D)
                .withBase(ItemStatistic.DAMAGE, 1_000D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.GOBLIN_HOLDOUT, 2));
    }
}
