package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Grandma Wolf's Kill Combo. Values {0}-{5} are how long the 5/10/15/20/25/30 combo lasts in
 * seconds; {6} is the Magic Find at 5 and 25 combo, {7} the coins per kill at 10 and 30, {8} the
 * Magic Find at 15 and {9} the Combat Wisdom at 20.
 */
public final class KillComboHandler implements PetAbilityHandler {
    private static final int[] THRESHOLDS = {5, 10, 15, 20, 25, 30};

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || !alive(runtime, context)) return ItemStatistics.empty();
        int combo = runtime.getStacks();
        double magicFind = 0;
        if (combo >= 5) magicFind += context.value(6);
        if (combo >= 15) magicFind += context.value(8);
        if (combo >= 25) magicFind += context.value(6);
        double wisdom = combo >= 20 ? context.value(9) : 0;
        return ItemStatistics.builder()
                .withBase(ItemStatistic.MAGIC_FIND, magicFind)
                .withBase(ItemStatistic.COMBAT_WISDOM, wisdom)
                .build();
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        if (!alive(runtime, context)) runtime.setStacks(0);
        runtime.setStacks(runtime.getStacks() + 1);
        runtime.setLastProc(System.currentTimeMillis());

        int coins = 0;
        if (runtime.getStacks() >= 10) coins += (int) context.value(7);
        if (runtime.getStacks() >= 30) coins += (int) context.value(7);
        if (coins > 0) kill.player().addCoins(coins);
    }

    private static boolean alive(AbilityRuntime runtime, PetAbilityContext context) {
        int combo = runtime.getStacks();
        if (combo <= 0) return false;
        double seconds = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (combo >= THRESHOLDS[i]) seconds = context.value(i);
        }
        if (seconds <= 0) seconds = context.value(0);
        return System.currentTimeMillis() - runtime.getLastProc() <= (long) (seconds * 1000);
    }
}
