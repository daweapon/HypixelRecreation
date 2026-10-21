package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.CreatureSpawner;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class QueenGuardSpecial implements CreatureSpecial {
    private static final String GUARD_ID = "ANT";
    private static final double GUARD_RADIUS = 40;

    @Override
    public boolean onPlayerHit(SkyBlockPlayer player, HuntingCreatureMob mob) {
        if (guarded(mob)) {
            SpecialMessages.send(player, mob.spec(), "ants_nearby");
            return true;
        }
        return VacuumGate.blocks(player);
    }

    static boolean guarded(HuntingCreatureMob queen) {
        double limit = GUARD_RADIUS * GUARD_RADIUS;
        return CreatureSpawner.alive(GUARD_ID).stream()
                .anyMatch(ant -> !ant.isDead() && !ant.isRemoved() && ant.getInstance() == queen.getInstance()
                        && ant.getPosition().distanceSquared(queen.getPosition()) <= limit);
    }
}
