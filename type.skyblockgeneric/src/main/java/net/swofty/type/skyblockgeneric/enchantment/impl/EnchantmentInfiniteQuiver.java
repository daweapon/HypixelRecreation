package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.catalogue.CatalogueEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
