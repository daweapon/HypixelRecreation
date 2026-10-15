package dev.daweapon.type.skyblockgeneric.reforge.abilities;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeAbility;

import java.util.Set;

public final class BloodSoakedAbility implements ReforgeAbility.EnchantLevelBonus {
    private static final Set<EnchantmentType> BOOSTED = Set.of(EnchantmentType.LIFE_STEAL,
            EnchantmentType.DRAIN, EnchantmentType.VAMPIRISM);

    @Override
    public String reforgeId() {
        return "blood_soaked";
    }

    @Override
    public int levelBonus(EnchantmentType type) {
        return BOOSTED.contains(type) ? 1 : 0;
    }
}
