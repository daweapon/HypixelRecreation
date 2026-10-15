package dev.daweapon.type.skyblockgeneric.gifts;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

public record GiftReward(GiftDropRarity rarity, int weight, Kind kind, long amount, @Nullable ItemType item) {

    public enum Kind {COINS, SKILL_XP, XP_BOOST_POTION, ITEM}

    public static GiftReward coins(GiftDropRarity rarity, int weight, long coins) {
        return new GiftReward(rarity, weight, Kind.COINS, coins, null);
    }

    public static GiftReward xp(GiftDropRarity rarity, int weight, long xp) {
        return new GiftReward(rarity, weight, Kind.SKILL_XP, xp, null);
    }

    public static GiftReward xpBoost(GiftDropRarity rarity, int weight, int potionLevel) {
        return new GiftReward(rarity, weight, Kind.XP_BOOST_POTION, potionLevel, null);
    }

    public static GiftReward item(GiftDropRarity rarity, int weight, ItemType item) {
        return new GiftReward(rarity, weight, Kind.ITEM, 1, item);
    }

    public double weight(double rareItemMultiplier) {
        return rarity.isRareItemTier() ? weight * rareItemMultiplier : weight;
    }
}
