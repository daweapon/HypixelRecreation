package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Skeleton's Combo: +3 Strength per stack while stacks last. {@link CombatPetHooks#bowHit} adds a
 * stack on every bow hit, up to {0}; they all fall off 8 seconds after the last hit.
 */
public final class SkeletonComboHandler implements PetAbilityHandler {
    static final long STACK_MS = 8_000;
    private static final double STRENGTH_PER_STACK = 3;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || runtime.getStacks() <= 0) return ItemStatistics.empty();
        if (System.currentTimeMillis() - runtime.getLastProc() > STACK_MS) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, STRENGTH_PER_STACK * runtime.getStacks()).build();
    }
}
