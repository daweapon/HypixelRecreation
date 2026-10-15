package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

/**
 * Guardian's Lazerbeam: at most every 3 seconds, the mob you hit is zapped for {0} times your
 * Intelligence.
 */
public final class LazerbeamHandler implements PetAbilityHandler {
    private static final long COOLDOWN_MS = 3_000;

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || context.runtime() == null || hit.mob().isDead()) return;
        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        if (now - runtime.getLastProc() < COOLDOWN_MS) return;
        double intelligence = hit.player().getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE);
        float damage = (float) (context.value(0) * Math.max(0, intelligence));
        if (damage <= 0) return;
        runtime.setLastProc(now);
        AttackService.applyHit(hit.player(), hit.mob(), damage, false);
    }
}
