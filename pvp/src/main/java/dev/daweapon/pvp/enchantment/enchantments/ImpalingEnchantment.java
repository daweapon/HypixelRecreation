package dev.daweapon.pvp.enchantment.enchantments;

import dev.daweapon.pvp.enchantment.CombatEnchantment;
import dev.daweapon.pvp.enchantment.EntityGroup;
import dev.daweapon.pvp.feature.config.FeatureConfiguration;
import dev.daweapon.pvp.feature.enchantment.EnchantmentFeature;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.enchant.Enchantment;

// Implements the Impaling enchantment, granting bonus attack damage against aquatic entities.
public class ImpalingEnchantment extends CombatEnchantment {
	public ImpalingEnchantment(EquipmentSlot... slotTypes) {
		super(Enchantment.IMPALING, slotTypes);
	}
	
	@Override
	public float getAttackDamage(int level, EntityGroup group,
	                             EnchantmentFeature feature, FeatureConfiguration configuration) {
		return group == EntityGroup.AQUATIC ? (float) level * 2.5F : 0.0F;
	}
}
