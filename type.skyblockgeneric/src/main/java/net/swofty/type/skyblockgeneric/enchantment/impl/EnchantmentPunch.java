package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import org.jetbrains.annotations.Nullable;

public class EnchantmentPunch extends CatalogueEnchantment {
    public static final float KNOCKBACK_PER_LEVEL = 0.6f;

    public EnchantmentPunch() {
        super("PUNCH");
    }

    public static float knockback(int level) {
        return KNOCKBACK_PER_LEVEL * level;
    }

    @Override
    protected @Nullable String limitation() {
        return "Knockback distance is approximate";
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
