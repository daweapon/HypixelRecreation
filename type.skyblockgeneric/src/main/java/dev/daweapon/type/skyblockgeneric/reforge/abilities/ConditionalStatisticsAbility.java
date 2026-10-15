package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeContext;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeWorld;

import java.util.function.Function;
import java.util.function.Predicate;

public record ConditionalStatisticsAbility(String reforgeId, Predicate<ReforgeWorld> when,
                                           Function<ReforgeWorld, ItemStatistics> grants)
        implements ReforgeAbility.Statistics {

    public static ConditionalStatisticsAbility always(String reforgeId, Function<ReforgeWorld, ItemStatistics> grants) {
        return new ConditionalStatisticsAbility(reforgeId, world -> true, grants);
    }

    public static ConditionalStatisticsAbility always(String reforgeId, ItemStatistics grants) {
        return always(reforgeId, world -> grants);
    }

    @Override
    public ItemStatistics statistics(ReforgeContext context) {
        return when.test(context.world()) ? grants.apply(context.world()) : ItemStatistics.empty();
    }
}
