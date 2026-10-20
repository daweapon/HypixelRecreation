package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class StarlynStatistics {
    private StarlynStatistics() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.empty();
        double sweepPercent = 0;
        for (StarlynSister sister : StarlynSister.values()) {
            StarlynProfile.SisterData data = StarlynContests.data(player, sister);
            total = total.addBase(ItemStatistic.FORAGING_FORTUNE,
                    (double) (data.getMilestoneGranted() - 1) * StarlynMilestones.FORAGING_FORTUNE_PER_MILESTONE);
            for (StarlynConfig.ShopEntry entry : StarlynConfig.get(sister).shop()) {
                switch (entry) {
                    case StarlynConfig.Power power -> total = total.addBase(ItemStatistic.SWEEP, StarlynShop.sweepFromPower(player, sister, power));
                    case StarlynConfig.Fortune fortune -> {
                        ItemStatistic statistic = fortune.statistic();
                        int level = data.purchased(fortune.id());
                        if (statistic != null && level > 0) total = total.addBase(statistic, (double) fortune.fortune(level));
                    }
                    case StarlynConfig.PersonalBest best -> {
                        if (data.purchased(best.id()) > 0) sweepPercent += StarlynPersonalBests.sweepPercent(data.personalBest(best.tree()));
                    }
                    default -> {
                    }
                }
            }
        }
        if (sweepPercent > 0) {
            total = ItemStatistics.add(total, ItemStatistics.builder().withMultiplicativePercentage(ItemStatistic.SWEEP, sweepPercent).build());
        }
        return total;
    }

    public static double helixFortune(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return 0;
        StarlynProfile.SisterData data = StarlynContests.data(player, StarlynSister.MIRIA);
        for (StarlynConfig.ShopEntry entry : StarlynConfig.get(StarlynSister.MIRIA).shop()) {
            if (entry instanceof StarlynConfig.Fortune fortune && fortune.tree() == StarlynTree.HELIX) {
                return fortune.fortune(data.purchased(fortune.id()));
            }
        }
        return 0;
    }
}
