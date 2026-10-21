package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class LassoFlyCharge implements CreatureSpecial {
    private static final double LASSO_STEP_SECONDS = 0.1;
    private final PerMob<StrikeFlight> flights = new PerMob<>();

    @Override
    public void onLassoTick(SkyBlockPlayer player, HuntingCreatureMob mob, long ticks) {
        flights.of(mob, () -> new StrikeFlight(new StrikeCycle(15, 20, 10), LASSO_STEP_SECONDS, 8, 14, 6))
                .advance(mob, player);
    }

    @Override
    public void onLassoDetach(SkyBlockPlayer player, HuntingCreatureMob mob) {
        flights.clear(mob);
    }
}
