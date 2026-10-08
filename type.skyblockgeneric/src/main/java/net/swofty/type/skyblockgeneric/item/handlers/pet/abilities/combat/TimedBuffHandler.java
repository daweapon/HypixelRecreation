package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Grants value {0} of one stat while a buff started by {@link CombatPetHooks} is running (Crow's
 * Camouflage after an ability, Enderman's Teleport Savvy after a Transmission).
 */
public final class TimedBuffHandler implements PetAbilityHandler {
    private final ItemStatistic statistic;

    public TimedBuffHandler(ItemStatistic statistic) {
        this.statistic = statistic;
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || System.currentTimeMillis() >= runtime.getBuffUntil()) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(statistic, context.value(0)).build();
    }
}
