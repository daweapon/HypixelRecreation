package net.swofty.type.skyblockgeneric.entity.mob.mobs.crystalhollows;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobHoldoutFlamethrower extends MobHoldoutGoblin {
    @Override
    public Integer getLevel() {
        return 65;
    }

    @Override
    public String getMobID() {
        return "GOBLIN_HOLDOUT_FLAMETHROWER";
    }

    @Override
    public String getDisplayName() {
        return "Goblin Flamethrower";
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 20_000D)
                .withBase(ItemStatistic.DAMAGE, 500D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(RegionType.GOBLIN_HOLDOUT, 1));
    }
}
