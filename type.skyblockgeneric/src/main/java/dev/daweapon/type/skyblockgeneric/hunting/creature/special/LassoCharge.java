package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class LassoCharge {
    static final double CONTACT = 1.7;
    private static final double LASSO_STEP_SECONDS = 0.1;
    private static final double WALK_SPEED = 9;
    private static final double FLY_SPEED = 8;

    private LassoCharge() {
    }

    static boolean step(HuntingCreatureMob mob, SkyBlockPlayer player, boolean flying) {
        if (mob.getPosition().distance(player.getPosition()) <= CONTACT) return true;
        if (flying) CreatureMotion.fly(mob, player.getPosition().add(0, 1, 0), FLY_SPEED * LASSO_STEP_SECONDS);
        else CreatureMotion.walk(mob, player.getPosition(), WALK_SPEED * LASSO_STEP_SECONDS);
        return false;
    }
}
