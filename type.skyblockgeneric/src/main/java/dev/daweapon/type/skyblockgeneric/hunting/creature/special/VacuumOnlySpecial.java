package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class VacuumOnlySpecial implements CreatureSpecial {
    @Override
    public boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
        return VacuumGate.blocks(player);
    }
}
