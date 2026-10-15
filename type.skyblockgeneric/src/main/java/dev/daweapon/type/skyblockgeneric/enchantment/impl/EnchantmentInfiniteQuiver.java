package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class EnchantmentInfiniteQuiver extends CatalogueEnchantment {
    public static final double SAVE_PERCENT_PER_LEVEL = 3;

    public EnchantmentInfiniteQuiver() {
        super("INFINITE_QUIVER");
    }

    public static double savePercent(int level) {
        return level * SAVE_PERCENT_PER_LEVEL;
    }

    public static boolean saves(SkyBlockPlayer player, SkyBlockItem bow) {
        SkyBlockEnchantment enchantment = bow.getAttributeHandler().getEnchantment(EnchantmentType.INFINITE_QUIVER);
        return enchantment != null && LootRoll.roll(player.getUuid(), null, savePercent(enchantment.level()));
    }

    @Override
    public boolean isEffectActive() {
        return true;
    }
}
