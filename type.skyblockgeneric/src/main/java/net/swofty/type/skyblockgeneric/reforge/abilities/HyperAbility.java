package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;

public final class HyperAbility implements ReforgeAbility.TransmissionBoost {
    @Override
    public String reforgeId() {
        return "hyper";
    }

    @Override
    public ItemStatistics boost() {
        return ItemStatistics.builder().withBase(ItemStatistic.SPEED, 5D).build();
    }

    @Override
    public long durationMillis() {
        return 5000;
    }
}
