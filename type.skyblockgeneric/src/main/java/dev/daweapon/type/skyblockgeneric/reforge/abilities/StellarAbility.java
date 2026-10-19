package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeContext;

public final class StellarAbility implements ReforgeAbility.Statistics, ReforgeAbility.StarPowder {
    private static final double MINING_FORTUNE = 75;

    @Override
    public String reforgeId() {
        return "stellar";
    }

    @Override
    public ItemStatistics statistics(ReforgeContext context) {
        return context.world().nearFallenStar() ? ItemStatistics.empty().addBase(ItemStatistic.MINING_FORTUNE, MINING_FORTUNE) : ItemStatistics.empty();
    }

    @Override
    public double starPowderPercent() {
        return 20;
    }
}
