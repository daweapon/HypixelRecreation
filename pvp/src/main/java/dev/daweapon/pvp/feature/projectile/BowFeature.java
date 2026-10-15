package dev.daweapon.pvp.feature.projectile;

import dev.daweapon.pvp.feature.CombatFeature;

/**
 * Combat feature which handles bow shooting.
 */
public interface BowFeature extends CombatFeature {
	BowFeature NO_OP = new BowFeature() {};
}
