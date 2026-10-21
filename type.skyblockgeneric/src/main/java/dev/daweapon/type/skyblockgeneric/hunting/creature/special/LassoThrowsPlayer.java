package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Vec;

final class LassoThrowsPlayer implements CreatureSpecial {
    private static final double WALK_SPEED = 5;
    private static final double LASSO_STEP_SECONDS = 0.1;
    private static final double BITE_RANGE = 2.2;
    private static final long BITE_COOLDOWN_MILLIS = 1500;
    private static final long TOSS_PERIOD = 30;
    private static final double LURE = 6;
    private static final double SIDEWAYS = 9;
    private static final double LIFT = 7;

    @Override
    public void onLassoTick(SkyBlockPlayer player, HuntingCreatureMob mob, long ticks) {
        double distance = mob.getPosition().distance(player.getPosition());
        if (distance > BITE_RANGE) CreatureMotion.walk(mob, player.getPosition(), WALK_SPEED * LASSO_STEP_SECONDS);
        else CreatureMotion.strike(mob, player, BITE_COOLDOWN_MILLIS);
        if (ticks % TOSS_PERIOD > 1) return;
        player.setVelocity(toss(CreatureMotion.horizontal(player.getPosition(), mob.getPosition()), ticks / TOSS_PERIOD));
    }

    static Vec toss(Vec towardMob, long round) {
        Vec side = new Vec(-towardMob.z(), 0, towardMob.x()).mul(round % 2 == 0 ? SIDEWAYS : -SIDEWAYS);
        Vec lure = towardMob.mul(LURE);
        return new Vec(side.x() + lure.x(), LIFT, side.z() + lure.z());
    }
}
