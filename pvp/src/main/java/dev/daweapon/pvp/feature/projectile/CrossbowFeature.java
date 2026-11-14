package dev.daweapon.pvp.feature.projectile;

import dev.daweapon.pvp.feature.CombatFeature;

/**
 * Combat feature which handles crossbow shooting.
 */
public interface CrossbowFeature extends CombatFeature {
	CrossbowFeature NO_OP = new CrossbowFeature() {};
}
