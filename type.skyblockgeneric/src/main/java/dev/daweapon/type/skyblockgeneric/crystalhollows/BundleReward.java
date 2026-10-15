package dev.daweapon.type.skyblockgeneric.crystalhollows;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public record BundleReward(String id, String name, double weight, ItemType item, int amount, EssenceType essence,
                           EnchantmentType enchantment, int enchantmentLevel, long meterXp) {

    public static BundleReward ofItem(String id, String name, double weight, ItemType item, int amount, long meterXp) {
        return new BundleReward(id, name, weight, item, amount, null, null, 0, meterXp);
    }

    public static BundleReward ofEssence(String id, String name, double weight, EssenceType essence, int amount) {
        return new BundleReward(id, name, weight, null, amount, essence, null, 0, 0);
    }

    public static BundleReward ofBook(String id, String name, double weight, EnchantmentType enchantment, int level, long meterXp) {
        return new BundleReward(id, name, weight, ItemType.ENCHANTED_BOOK, 1, null, enchantment, level, meterXp);
    }

    public boolean hasMeter() {
        return meterXp > 0;
    }

    public void give(SkyBlockPlayer player) {
        if (essence != null) {
            Essences.add(player, essence, amount);
            return;
        }
        SkyBlockItem reward = new SkyBlockItem(item, amount);
        if (enchantment != null) {
            reward.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(enchantment, enchantmentLevel));
        }
        player.addAndUpdateItem(reward);
    }
}
