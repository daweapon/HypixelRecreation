package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Golem's Last Stand: below 20% health incoming damage is cut by 20%, and dropping that low raises
 * a shield of 40% max health plus 40% more damage for 12 seconds, once a minute.
 */
public final class LastStandHandler implements PetAbilityHandler {
    private static final double LOW_HEALTH = 0.2;
    private static final double REDUCTION = 0.2;
    private static final double SHIELD = 0.4;
    private static final double DAMAGE_PERCENT = 40;
    private static final long DURATION_MS = 12_000;
    private static final long COOLDOWN_MS = 60_000;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        AbilityRuntime runtime = context.runtime();
        if (runtime == null || System.currentTimeMillis() >= runtime.getBuffUntil()) return ItemStatistics.empty();
        return ItemStatistics.builder().withAdditivePercentage(ItemStatistic.DAMAGE, DAMAGE_PERCENT).build();
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.DamagedByMob hit) || context.runtime() == null) return;
        SkyBlockPlayer player = hit.player();
        double threshold = player.getMaxHealth() * LOW_HEALTH;
        if (player.getHealth() < threshold) hit.damage(hit.damage() * (1 - REDUCTION));
        if (player.getHealth() - hit.damage() >= threshold) return;

        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        if (runtime.getLastProc() != 0 && now - runtime.getLastProc() < COOLDOWN_MS) return;
        runtime.setLastProc(now);
        runtime.setBuffUntil(now + DURATION_MS);

        float shield = (float) (player.getMaxHealth() * SHIELD);
        player.setAdditionalHearts(player.getAdditionalHearts() + shield);
        MinecraftServer.getSchedulerManager().scheduleTask(
                () -> player.setAdditionalHearts(Math.max(0, player.getAdditionalHearts() - shield)),
                TaskSchedule.millis(DURATION_MS), TaskSchedule.stop());
    }
}
