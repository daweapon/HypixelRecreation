package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.hunting.creature.LassoService;
import net.minestom.server.coordinate.Vec;

final class Flee implements CreatureSpecial {
    private static final double SCARE_RANGE = 8;
    private static final double RUN_DISTANCE = 10;
    private static final double WALK_PER_TICK = 0.35;
    private static final long REPATH_TICKS = 10;

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (LassoService.isLassoed(mob)) return;
        CreatureMotion.nearestPlayer(mob, SCARE_RANGE).ifPresent(player -> {
            Vec away = CreatureMotion.horizontal(player.getPosition(), mob.getPosition());
            var exit = mob.getPosition().add(away.x() * RUN_DISTANCE, 0, away.z() * RUN_DISTANCE);
            if (ticks % REPATH_TICKS == 0 && mob.getVanillaNavigator().pathTo(exit)) return;
            if (mob.getVanillaNavigator().isComplete()) CreatureMotion.walk(mob, exit, WALK_PER_TICK);
        });
    }
}
