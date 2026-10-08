package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Turtle's Turtle Shell: under 40% health mob damage is cut by {0}%, and after 10 hits taken the
 * player gains +{1} Vitality until 10 seconds pass without a hit.
 */
public final class TurtleShellHandler implements PetAbilityHandler {
    private static final double LOW_HEALTH = 0.4;
    private static final int HITS = 10;
    private static final long RESET_MS = 10_000;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || runtime.getHits() < HITS) return ItemStatistics.empty();
        if (System.currentTimeMillis() - runtime.getLastProc() > RESET_MS) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.VITALITY, context.value(1)).build();
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.DamagedByMob hit) || context.runtime() == null) return;
        SkyBlockPlayer player = hit.player();
        if (player.getHealth() < player.getMaxHealth() * LOW_HEALTH) {
            double percent = Math.max(0, Math.min(100, context.value(0)));
            hit.damage(hit.damage() * (1 - percent / 100D));
        }
        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        if (now - runtime.getLastProc() > RESET_MS) runtime.setHits(0);
        runtime.setHits(runtime.getHits() + 1);
        runtime.setLastProc(now);
    }
}
