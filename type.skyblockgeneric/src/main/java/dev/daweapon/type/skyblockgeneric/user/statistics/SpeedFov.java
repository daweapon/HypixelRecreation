package dev.daweapon.type.skyblockgeneric.user.statistics;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;

/**
 * The client widens the FOV by its movement speed over the abilities "walking speed", which moves nothing else.
 * Keeping that walking speed equal to the player's movement speed leaves the FOV at the player's own setting.
 */
public final class SpeedFov {
    private static final float TOLERANCE = 1.0E-4f;

    private SpeedFov() {
    }

    public static void hold(Player player) {
        float target = (float) player.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
        if (target <= 0 || Math.abs(target - player.getFieldViewModifier()) < TOLERANCE) return;
        player.setFieldViewModifier(target);
    }
}
