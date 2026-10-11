package net.swofty.type.skyblockgeneric.enchantment;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

// Builds enchanted books, including the full library of every enchantment at its top level.
public final class EnchantmentBooks {
    private EnchantmentBooks() {}

    public static SkyBlockItem book(EnchantmentType type, int level) {
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(type, level));
        return book;
    }

    public static int topLevel(EnchantmentType type, SkyBlockPlayer player) {
        return Math.max(type.getEnch().getLevelsToApply(player).maximumLevel(), type.getEnch().getEndcapLevel());
    }

    public static List<SkyBlockItem> library(SkyBlockPlayer player) {
        return Arrays.stream(EnchantmentType.values()).map(type -> book(type, topLevel(type, player))).toList();
    }
}
