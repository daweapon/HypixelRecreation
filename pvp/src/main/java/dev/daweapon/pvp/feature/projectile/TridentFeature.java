package dev.daweapon.pvp.feature.projectile;

import dev.daweapon.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;

/**
 * Combat feature which handles using a trident.
 */
public interface TridentFeature extends CombatFeature {
	TridentFeature NO_OP = (player, level) -> {};
	
	void applyRiptide(Player player, int level);
}
