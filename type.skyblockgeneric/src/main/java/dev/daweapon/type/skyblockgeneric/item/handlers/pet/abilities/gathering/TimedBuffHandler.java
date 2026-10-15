package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

import java.util.function.Predicate;

/**
 * When an event matches, grants value {i} of the i-th statistic for a fixed time. A new match
 * restarts the timer.
 */
public final class TimedBuffHandler implements PetAbilityHandler {
    private final Predicate<PetEvent> trigger;
    private final long durationMillis;
    private final ItemStatistic[] statistics;

    public TimedBuffHandler(Predicate<PetEvent> trigger, long durationMillis, ItemStatistic... statistics) {
        this.trigger = trigger;
        this.durationMillis = durationMillis;
        this.statistics = statistics;
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || !trigger.test(event)) return;
        runtime.setBuffUntil(System.currentTimeMillis() + durationMillis);
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || System.currentTimeMillis() >= runtime.getBuffUntil()) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (int i = 0; i < statistics.length; i++) builder.withBase(statistics[i], context.value(i));
        return builder.build();
    }
}
