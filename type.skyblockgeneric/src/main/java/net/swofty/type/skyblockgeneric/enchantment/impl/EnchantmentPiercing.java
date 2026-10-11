package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;

public class EnchantmentPiercing extends CatalogueEnchantment {
    public static final double EXTRA_TARGET_DAMAGE_FACTOR = 0.25;

    public EnchantmentPiercing() {
        super("PIERCING");
    }

    public static double damageFactor(int targetsAlreadyHit) {
        return targetsAlreadyHit == 0 ? 1 : EXTRA_TARGET_DAMAGE_FACTOR;
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
