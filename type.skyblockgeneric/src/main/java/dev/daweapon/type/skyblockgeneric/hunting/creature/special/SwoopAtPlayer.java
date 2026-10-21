package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.hunting.creature.LassoService;

final class SwoopAtPlayer implements CreatureSpecial {
    private static final double AWARE_RANGE = 20;
    private static final double TICK_SECONDS = 0.05;
    private final PerMob<StrikeFlight> flights = new PerMob<>();

    @Override
    public void onTick(HuntingCreatureMob mob, long ticks) {
        if (LassoService.isLassoed(mob)) return;
        CreatureMotion.nearestPlayer(mob, AWARE_RANGE).ifPresentOrElse(
                player -> flights.of(mob, () -> new StrikeFlight(new StrikeCycle(80, 40, 30), TICK_SECONDS, 6, 14, 6)).advance(mob, player),
                () -> flights.clear(mob));
    }
}
