package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Golden Dragon's Legendary Treasure: +{0}% damage for every million coins in the bank, up to +{1}%.
 */
public final class LegendaryTreasureHandler implements PetAbilityHandler {
    private static final double MILLION = 1_000_000D;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        double bank = PersonalBankService.data(context.player()).getAmount();
        double percent = Math.min(context.value(1), context.value(0) * Math.floor(bank / MILLION));
        if (percent <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withAdditivePercentage(ItemStatistic.DAMAGE, percent).build();
    }
}
