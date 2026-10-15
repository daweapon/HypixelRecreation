package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Giraffe's Higher Ground: +{0}% Crit Damage and Strength for every 0.1 Swing Range above 3, up
 * to 6.
 */
public final class HigherGroundHandler implements PetAbilityHandler {
    private static final double FROM = 3;
    private static final double TO = 6;
    private static final double STEP = 0.1;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        double range = context.player().getStatistics().allStatistics().getOverall(ItemStatistic.SWING_RANGE);
        double steps = Math.floor((Math.min(TO, range) - FROM) / STEP + 1e-9);
        if (steps <= 0) return ItemStatistics.empty();
        double percent = steps * context.value(0);
        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.CRITICAL_DAMAGE, percent)
                .withAdditivePercentage(ItemStatistic.STRENGTH, percent)
                .build();
    }
}
