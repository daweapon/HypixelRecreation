package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

final class StrikeFlight {
    private static final double ORBIT_RADIUS = 5;
    private static final double ORBIT_HEIGHT = 3;
    private static final double REACH = 1.6;
    private static final double RETREAT_CLIMB = 4;
    private static final long STRIKE_COOLDOWN_MILLIS = 800;

    private final StrikeCycle cycle;
    private final double stepSeconds;
    private final double orbitSpeed;
    private final double diveSpeed;
    private final double retreatSpeed;

    StrikeFlight(StrikeCycle cycle, double stepSeconds, double orbitSpeed, double diveSpeed, double retreatSpeed) {
        this.cycle = cycle;
        this.stepSeconds = stepSeconds;
        this.orbitSpeed = orbitSpeed;
        this.diveSpeed = diveSpeed;
        this.retreatSpeed = retreatSpeed;
    }

    void advance(HuntingCreatureMob mob, SkyBlockPlayer player) {
        Point body = player.getPosition().add(0, 1, 0);
        boolean reached = mob.getPosition().distance(body) <= REACH;
        switch (cycle.phase()) {
            case ORBIT -> {
                double angle = cycle.orbitAngle();
                Point spot = player.getPosition().add(Math.cos(angle) * ORBIT_RADIUS, ORBIT_HEIGHT, Math.sin(angle) * ORBIT_RADIUS);
                CreatureMotion.fly(mob, spot, orbitSpeed * stepSeconds);
            }
            case DIVE -> {
                CreatureMotion.fly(mob, body, diveSpeed * stepSeconds);
                if (reached) CreatureMotion.strike(mob, player, STRIKE_COOLDOWN_MILLIS);
            }
            case RECOVER -> {
                Vec away = CreatureMotion.horizontal(player.getPosition(), mob.getPosition());
                Point exit = mob.getPosition().add(away.x() * RETREAT_CLIMB, RETREAT_CLIMB, away.z() * RETREAT_CLIMB);
                CreatureMotion.fly(mob, exit, retreatSpeed * stepSeconds);
            }
        }
        cycle.tick(reached);
    }

    void reset() {
        cycle.reset();
    }
}
