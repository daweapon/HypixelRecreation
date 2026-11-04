package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ChocolateFortune {
    public static final int MAX_TIERS = 25;
    public static final long COST = 2_000_000_000L;
    public static final int REQUIRED_PRESTIGE = 5;
    public static final double FORTUNE_PER_TIER = 1;

    private ChocolateFortune() {
    }

    public enum Result {
        SUCCESS, MAXED, NOT_ENOUGH_CHOCOLATE
    }

    public static Result buy(SkyBlockPlayer player) {
        ChocolateFactoryData current = ChocolateFactoryHelper.getData(player);
        if (current.getChocolateFortune() >= MAX_TIERS) return Result.MAXED;
        if (current.getChocolate() < COST) return Result.NOT_ENOUGH_CHOCOLATE;
        ChocolateFactoryHelper.mutateData(player, data -> {
            data.removeChocolate(COST);
            data.addChocolateSpent(COST);
            data.setChocolateFortune(data.getChocolateFortune() + 1);
        });
        return Result.SUCCESS;
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        int tiers = ChocolateFactoryHelper.getData(player).getChocolateFortune();
        if (tiers <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.COCOA_BEANS_FORTUNE, tiers * FORTUNE_PER_TIER).build();
    }
}
