package dev.daweapon.type.hub.gui.elizabeth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;

import java.util.Map;

@AllArgsConstructor
@Getter
public class CommunityShopItem {
    private static final Map<ItemType, EnchantmentType> STACKING_ENCHANT_BOOKS = Map.of(
            ItemType.CHAMPION, EnchantmentType.CHAMPION,
            ItemType.COMPACT, EnchantmentType.COMPACT,
            ItemType.CULTIVATING, EnchantmentType.CULTIVATING,
            ItemType.EXPERTISE, EnchantmentType.EXPERTISE,
            ItemType.HECATOMB, EnchantmentType.HECATOMB);

    private ItemType itemType;
    private int price;
    private int amount;

    public SkyBlockItem createItem() {
        EnchantmentType enchantment = STACKING_ENCHANT_BOOKS.get(itemType);
        if (enchantment == null) return new SkyBlockItem(itemType);
        SkyBlockItem book = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        book.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(enchantment, 1));
        return book;
    }
}
