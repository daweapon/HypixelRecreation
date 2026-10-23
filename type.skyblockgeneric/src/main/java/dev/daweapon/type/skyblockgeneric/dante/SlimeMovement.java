package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;

final class SlimeMovement {
    private SlimeMovement() {
    }

    static void hop(EntityCreature slime, Player target, double speed, double lift) {
        Vec toward = target.getPosition().sub(slime.getPosition()).asVec().withY(0);
        Vec heading = toward.length() < 0.001 ? Vec.ZERO : toward.normalize().mul(speed);
        slime.setVelocity(heading.withY(lift));
    }
}
