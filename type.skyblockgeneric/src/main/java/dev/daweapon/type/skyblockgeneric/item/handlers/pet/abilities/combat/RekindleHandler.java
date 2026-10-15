package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Phoenix's Rekindle: a hit that would kill instead makes the player immune and grants +{0}
 * Strength for {1} seconds, once a minute.
 */
public final class RekindleHandler implements PetAbilityHandler {
    private static final long COOLDOWN_MS = 60_000;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || System.currentTimeMillis() >= runtime.getBuffUntil()) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.STRENGTH, context.value(0)).build();
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        switch (event) {
            case PetEvent.DamagedByMob hit -> {
                if (rekindles(hit.player(), hit.damage(), runtime, now, context.value(1))) hit.damage(0);
            }
            case PetEvent.FallDamage fall -> {
                if (rekindles(fall.player(), fall.damage(), runtime, now, context.value(1))) fall.damage(0);
            }
            default -> {
            }
        }
    }

    private static boolean rekindles(SkyBlockPlayer player, double damage, AbilityRuntime runtime, long now,
                                     double seconds) {
        if (now < runtime.getBuffUntil()) return true;
        if (damage < player.getHealth() + player.getAdditionalHearts()) return false;
        if (runtime.getLastProc() != 0 && now - runtime.getLastProc() < COOLDOWN_MS) return false;
        runtime.setLastProc(now);
        runtime.setBuffUntil(now + (long) (seconds * 1000));
        return true;
    }
}
