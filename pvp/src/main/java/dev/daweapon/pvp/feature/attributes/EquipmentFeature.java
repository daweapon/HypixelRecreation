package dev.daweapon.pvp.feature.attributes;

import dev.daweapon.pvp.feature.CombatFeature;

/**
 * Combat feature which handles equipment changes (applies weapon and armor attributes).
 */
public interface EquipmentFeature extends CombatFeature {
	EquipmentFeature NO_OP = new EquipmentFeature() {};
}
