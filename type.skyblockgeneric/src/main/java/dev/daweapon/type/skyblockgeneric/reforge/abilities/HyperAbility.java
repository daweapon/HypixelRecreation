package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

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
