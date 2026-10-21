package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class GrizzlyLassoThenBlackHole implements CreatureSpecial {
    @Override
    public boolean lassoable() {
        return true;
    }

    @Override
    public boolean onLassoReeled(SkyBlockPlayer player, HuntingCreatureMob mob) {
        CreatureMotion.say(mob, player, "lasso_destroyed");
        return true;
    }

    @Override
    public void onLassoTick(SkyBlockPlayer player, HuntingCreatureMob mob, long ticks) {
        if (!LassoCharge.step(mob, player, false)) return;
        CreatureMotion.strike(mob, player, 0);
        CreatureMotion.say(mob, player, "broke_free");
        LassoRelease.release(player, mob);
    }

    @Override
    public boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
        return mob.atHuntFloor();
    }
}
