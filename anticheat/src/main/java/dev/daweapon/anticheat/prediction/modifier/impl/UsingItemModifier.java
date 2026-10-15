package dev.daweapon.anticheat.prediction.modifier.impl;

import dev.daweapon.anticheat.math.Vel;
import dev.daweapon.anticheat.prediction.PlayerContext;
import dev.daweapon.anticheat.prediction.modifier.VelocityModifier;

/**
 * Using item modifier (eating, drinking, blocking)
 * Slows movement to 20% of normal speed
 */
public class UsingItemModifier extends VelocityModifier {

    private static final double USING_ITEM_MULTIPLIER = 0.2;

    @Override
    public Vel apply(Vel currentVel, PlayerContext context) {
        return new Vel(
            currentVel.x() * USING_ITEM_MULTIPLIER,
            currentVel.y(),
            currentVel.z() * USING_ITEM_MULTIPLIER
        );
    }

    @Override
    public boolean shouldApply(PlayerContext context) {
        return context.isUsingItem();
    }

    @Override
    public int getPriority() {
        return 95;
    }

    @Override
    public String getName() {
        return "Using Item";
    }
}
