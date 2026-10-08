package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Golden Dragon's Dragon's Greed: +{0}% Strength for every 5 Magic Find, up to +{1}%.
 */
public final class DragonsGreedHandler implements PetAbilityHandler {
    private static final double MAGIC_FIND_STEP = 5;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        double magicFind = context.player().getStatistics().allStatistics().getOverall(ItemStatistic.MAGIC_FIND);
        double percent = Math.min(context.value(1), context.value(0) * Math.floor(magicFind / MAGIC_FIND_STEP));
        if (percent <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withAdditivePercentage(ItemStatistic.STRENGTH, percent).build();
    }
}
