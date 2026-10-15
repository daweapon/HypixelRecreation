package dev.daweapon.pvp.feature.projectile;

import dev.daweapon.pvp.feature.CombatFeature;

/**
 * Combat feature which handles using fireworks.
 */
public interface FireworkRocketFeature extends CombatFeature {
    FireworkRocketFeature NO_OP = new FireworkRocketFeature() {
    };
}