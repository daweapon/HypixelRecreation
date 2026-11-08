package dev.daweapon.type.skyblockgeneric.user.statistics;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;

/**
 * The client widens the FOV by its movement speed over the abilities "walking speed", which moves nothing else.
 * Matching that walking speed to the speed the client works out (base speed times the vanilla Speed and Slowness
 * multipliers) keeps every bit of the speed while the FOV stays put.
 */
public final class SpeedFov {
    private static final double SPEED_PER_LEVEL = 0.2;
    private static final double SLOWNESS_PER_LEVEL = 0.15;
    private static final float TOLERANCE = 1.0E-4f;

    private SpeedFov() {
    }

    public static void hold(Player player) {
        float target = (float) clientSpeed(player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue(),
                level(player, PotionEffect.SPEED), level(player, PotionEffect.SLOWNESS));
        if (target <= 0 || Math.abs(target - player.getFieldViewModifier()) < TOLERANCE) return;
        player.setFieldViewModifier(target);
    }

    static double clientSpeed(double base, int speedLevel, int slownessLevel) {
        return base * Math.max(0, 1 + SPEED_PER_LEVEL * speedLevel - SLOWNESS_PER_LEVEL * slownessLevel);
    }

    private static int level(Player player, PotionEffect effect) {
        for (TimedPotion potion : player.getActiveEffects()) {
            if (potion.potion().effect() == effect) return potion.potion().amplifier() + 1;
        }
        return 0;
    }
}
