package dev.daweapon.pvp.feature.potion;

import dev.daweapon.pvp.feature.CombatFeature;

/**
 * Combat feature which handles drinking and throwing potions.
 */
public interface PotionFeature extends CombatFeature {
	PotionFeature NO_OP = new PotionFeature() {};
}
