package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.ToIntFunction;

/**
 * Grants value {i} of the i-th statistic for each unit the player has of something: a level, a
 * count of owned things, digits of a collection. Percentage mode adds an additive percentage.
 */
public final class CountScaledStatHandler implements PetAbilityHandler {
    private final ToIntFunction<SkyBlockPlayer> count;
    private final boolean percentage;
    private final ItemStatistic[] statistics;

    private CountScaledStatHandler(ToIntFunction<SkyBlockPlayer> count, boolean percentage, ItemStatistic... statistics) {
        this.count = count;
        this.percentage = percentage;
        this.statistics = statistics;
    }

    public static CountScaledStatHandler flat(ToIntFunction<SkyBlockPlayer> count, ItemStatistic... statistics) {
        return new CountScaledStatHandler(count, false, statistics);
    }

    public static CountScaledStatHandler percent(ToIntFunction<SkyBlockPlayer> count, ItemStatistic... statistics) {
        return new CountScaledStatHandler(count, true, statistics);
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        int units = count.applyAsInt(player);
        if (units <= 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (int i = 0; i < statistics.length; i++) {
            double value = context.value(i) * units;
            if (value == 0) continue;
            if (percentage) builder.withAdditivePercentage(statistics[i], value);
            else builder.withBase(statistics[i], value);
        }
        return builder.build();
    }
}
