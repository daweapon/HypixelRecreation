package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;

public class EnchantmentSnipe extends CatalogueEnchantment {
    public static final double PERCENT_PER_LEVEL = 1;
    public static final double BLOCKS_PER_STEP = 10;

    public EnchantmentSnipe() {
        super("SNIPE");
    }

    public static double multiplier(int level, double blocksTravelled) {
        double steps = Math.floor(Math.max(0, blocksTravelled) / BLOCKS_PER_STEP);
        return 1 + level * PERCENT_PER_LEVEL * steps / 100D;
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
