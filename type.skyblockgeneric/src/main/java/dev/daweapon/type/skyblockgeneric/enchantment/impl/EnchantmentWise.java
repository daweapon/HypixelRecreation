package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class EnchantmentWise extends CatalogueEnchantment {
    public static final double REDUCTION_PERCENT_PER_LEVEL = 10;

    public EnchantmentWise() {
        super("WISE");
    }

    public static double costMultiplier(int level) {
        return Math.max(0, 1 - level * REDUCTION_PERCENT_PER_LEVEL / 100D);
    }

    public static double costMultiplier(SkyBlockPlayer player) {
        SkyBlockEnchantment wise = new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler()
                .getEnchantment(EnchantmentType.WISE);
        return wise == null ? 1 : costMultiplier(wise.level());
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
