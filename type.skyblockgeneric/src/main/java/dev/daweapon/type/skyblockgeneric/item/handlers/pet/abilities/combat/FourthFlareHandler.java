package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

/**
 * Phoenix's Fourth Flare: every 4th melee hit ignites the mob for {0}x the player's Crit Damage
 * each second, for {1} seconds.
 */
public final class FourthFlareHandler implements PetAbilityHandler {
    private static final int HITS = 4;
    private static final int TICKS_PER_SECOND = 20;

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        runtime.setHits(runtime.getHits() + 1);
        if (runtime.getHits() < HITS) return;
        runtime.setHits(0);

        SkyBlockPlayer player = hit.player();
        SkyBlockMob mob = hit.mob();
        double critDamage = player.getStatistics().allStatistics().getOverall(ItemStatistic.CRITICAL_DAMAGE);
        float perSecond = (float) (context.value(0) * critDamage);
        int seconds = (int) Math.floor(context.value(1));
        if (perSecond <= 0) return;
        mob.setFireTicks(seconds * TICKS_PER_SECOND);
        for (int second = 1; second <= seconds; second++) {
            MinecraftServer.getSchedulerManager().scheduleTask(
                    () -> AttackService.applyHit(player, mob, perSecond, false),
                    TaskSchedule.tick(second * TICKS_PER_SECOND), TaskSchedule.stop());
        }
    }
}
