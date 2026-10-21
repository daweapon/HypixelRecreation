package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpec;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class OnlyFromDistanceSpecial implements CreatureSpecial {
    private final double minimumDistance;

    OnlyFromDistanceSpecial(double minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    @Override
    public boolean onNetHit(SkyBlockPlayer player, HuntingCreatureMob mob, CreatureSpec.NetTier tier, double distance) {
        if (distance >= minimumDistance) return false;
        player.sendMessage("<c>The <f>{}<c> can only be hit from at least {} blocks away!", mob.spec().name(), (int) minimumDistance);
        return true;
    }
}
