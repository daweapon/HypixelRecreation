package net.swofty.type.skyblockgeneric.reforge.abilities;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.reforge.ReforgeAbility;
import net.swofty.type.skyblockgeneric.reforge.ReforgeContext;
import net.swofty.type.skyblockgeneric.reforge.ReforgeWorld;

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
