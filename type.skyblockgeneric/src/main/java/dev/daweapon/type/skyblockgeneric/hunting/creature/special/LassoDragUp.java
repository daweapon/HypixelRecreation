package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Vec;

final class LassoDragUp implements CreatureSpecial {
    private static final double CLIMB_SPEED = 6;
    private static final double LASSO_STEP_SECONDS = 0.1;
    private static final double LEAD = 6;
    private static final double PLAYER_LIFT = 8;

    @Override
    public void onLassoTick(SkyBlockPlayer player, HuntingCreatureMob mob, long ticks) {
        if (mob.getPosition().y() - player.getPosition().y() < LEAD) {
            CreatureMotion.fly(mob, mob.getPosition().add(0, 1, 0), CLIMB_SPEED * LASSO_STEP_SECONDS);
        } else {
            mob.setVelocity(Vec.ZERO);
        }
        player.setVelocity(new Vec(player.getVelocity().x(), PLAYER_LIFT, player.getVelocity().z()));
    }
}
