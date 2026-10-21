package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Vec;

import java.util.Set;

final class LassoChargeBreaks implements CreatureSpecial {
    private static final Set<String> FLYING = Set.of("STAG_BEETLE");
    private static final Set<String> KNOCKBACK = Set.of("MOUNTAIN_GOAT");
    private static final double KNOCKBACK_HORIZONTAL = 30;
    private static final double KNOCKBACK_VERTICAL = 12;

    @Override
    public void onLassoTick(SkyBlockPlayer player, HuntingCreatureMob mob, long ticks) {
        String id = mob.spec().id();
        if (!LassoCharge.step(mob, player, FLYING.contains(id))) return;
        CreatureMotion.say(mob, player, "lasso_broken");
        if (KNOCKBACK.contains(id)) {
            Vec away = CreatureMotion.horizontal(mob.getPosition(), player.getPosition());
            player.setVelocity(new Vec(away.x() * KNOCKBACK_HORIZONTAL, KNOCKBACK_VERTICAL, away.z() * KNOCKBACK_HORIZONTAL));
        }
        LassoRelease.release(player, mob);
    }
}
