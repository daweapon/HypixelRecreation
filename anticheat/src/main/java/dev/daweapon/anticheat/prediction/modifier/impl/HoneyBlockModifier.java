package dev.daweapon.anticheat.prediction.modifier.impl;

import dev.daweapon.anticheat.math.Vel;
import dev.daweapon.anticheat.prediction.PlayerContext;
import dev.daweapon.anticheat.prediction.modifier.VelocityModifier;

/**
 * Honey block speed reduction
 * Honey blocks slow movement similar to soul sand
 */
public class HoneyBlockModifier extends VelocityModifier {

    private static final double HONEY_SPEED_MULTIPLIER = 0.4;

    @Override
    public Vel apply(Vel currentVel, PlayerContext context) {
        return new Vel(
            currentVel.x() * HONEY_SPEED_MULTIPLIER,
            currentVel.y() * HONEY_SPEED_MULTIPLIER,
            currentVel.z() * HONEY_SPEED_MULTIPLIER
        );
    }

    @Override
    public boolean shouldApply(PlayerContext context) {
        return context.isOnHoney();
    }

    @Override
    public int getPriority() {
        return 65;
    }

    @Override
    public String getName() {
        return "Honey Block";
    }
}
