package net.swofty.type.skyblockgeneric.enchantment.abstr;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;

import java.util.List;

// Marks an enchantment as mutually exclusive with a set of other enchantment types.
public interface ConflictingEnch {
    List<EnchantmentType> getConflictingEnchantments();
}
