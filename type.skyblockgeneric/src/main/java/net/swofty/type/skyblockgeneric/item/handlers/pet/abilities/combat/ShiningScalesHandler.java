package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Golden Dragon's Shining Scales: Strength and Magic Find for each digit of the Gold collection,
 * counted up to 100M.
 */
public final class ShiningScalesHandler implements PetAbilityHandler {
    private static final double STRENGTH_PER_DIGIT = 11.1;
    private static final double MAGIC_FIND_PER_DIGIT = 2.2;
    private static final long MAX_COLLECTION = 100_000_000L;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        long gold = Math.min(MAX_COLLECTION, context.player().getCollection().get(ItemType.GOLD_INGOT));
        if (gold <= 0) return ItemStatistics.empty();
        int digits = Long.toString(gold).length();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.STRENGTH, STRENGTH_PER_DIGIT * digits)
                .withBase(ItemStatistic.MAGIC_FIND, MAGIC_FIND_PER_DIGIT * digits)
                .build();
    }
}
