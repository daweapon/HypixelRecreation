package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Horse's Stampede: each mob kill adds a stack of +{0} Speed and +{1} Strength for 5 seconds, up to
 * 20 stacks. A kill refreshes the timer for every stack.
 */
public final class HorseStampedeHandler implements PetAbilityHandler {
    private static final long DURATION_MS = 5_000;
    private static final int MAX_STACKS = 20;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || runtime.getStacks() <= 0 || System.currentTimeMillis() > runtime.getBuffUntil()) {
            return ItemStatistics.empty();
        }
        int stacks = runtime.getStacks();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.SPEED, context.value(0) * stacks)
                .withBase(ItemStatistic.STRENGTH, context.value(1) * stacks)
                .build();
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        int stacks = now > runtime.getBuffUntil() ? 0 : runtime.getStacks();
        runtime.setStacks(Math.min(MAX_STACKS, stacks + 1));
        runtime.setBuffUntil(now + DURATION_MS);
    }
}
