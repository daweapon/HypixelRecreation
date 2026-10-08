package net.swofty.type.skyblockgeneric.crimson.mob;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One row of a Crimson Isle mob's drop table. Items are named by id so a row whose item this server
 * doesn't have yet stays catalogued and starts dropping as soon as the item exists. A row with several
 * {@code choices} drops one of them (the Rampart Armor Piece). {@code magicFind} is false for the
 * miniboss rows Hypixel exempts from Magic Find.
 */
public record MobDrop(List<String> choices, int min, int max, double chancePercent, boolean magicFind,
                      @Nullable Rarity rarity, @Nullable String enchantment, int enchantmentLevel) {

    public static MobDrop always(String itemId, int min, int max) {
        return new MobDrop(List.of(itemId), min, max, 100, true, null, null, 0);
    }

    public static MobDrop always(String itemId, int amount) {
        return always(itemId, amount, amount);
    }

    public static MobDrop chance(String itemId, int amount, double chancePercent) {
        return new MobDrop(List.of(itemId), amount, amount, chancePercent, true, null, null, 0);
    }

    public static MobDrop chance(String itemId, double chancePercent) {
        return chance(itemId, 1, chancePercent);
    }

    public static MobDrop oneOf(double chancePercent, String... itemIds) {
        return new MobDrop(List.of(itemIds), 1, 1, chancePercent, true, null, null, 0);
    }

    public static MobDrop pet(String itemId, Rarity rarity, double chancePercent) {
        return new MobDrop(List.of(itemId), 1, 1, chancePercent, true, rarity, null, 0);
    }

    public static MobDrop book(String enchantment, int level, double chancePercent) {
        return new MobDrop(List.of("ENCHANTED_BOOK"), 1, 1, chancePercent, true, null, enchantment, level);
    }

    public static MobDrop cyclamen(double chancePercent) {
        return chance("DYE_CYCLAMEN", chancePercent);
    }

    public MobDrop noMagicFind() {
        return new MobDrop(choices, min, max, chancePercent, false, rarity, enchantment, enchantmentLevel);
    }

    public List<ItemType> availableItems() {
        if (enchantment != null && enchantmentType() == null) return List.of();
        return choices.stream().map(ItemType::get).filter(java.util.Objects::nonNull).toList();
    }

    public boolean isAvailable() {
        return !availableItems().isEmpty();
    }

    public @Nullable EnchantmentType enchantmentType() {
        if (enchantment == null) return null;
        try {
            return EnchantmentType.valueOf(enchantment);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int rollAmount() {
        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
