package dev.daweapon.type.skyblockgeneric.enchantment.abstr;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;

import java.util.List;

// Marks an enchantment as mutually exclusive with a set of other enchantment types.
public interface ConflictingEnch {
    List<EnchantmentType> getConflictingEnchantments();
}
