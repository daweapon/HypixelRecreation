package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class StarbornStatistics {
    private StarbornStatistics() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double fortune = StarbornBenefactors.willForagingFortune(player);
        return fortune > 0 ? ItemStatistics.empty().addBase(ItemStatistic.FORAGING_FORTUNE, fortune) : ItemStatistics.empty();
    }
}
