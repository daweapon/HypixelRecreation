package dev.daweapon.type.skyblockgeneric.gifts;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static dev.daweapon.type.skyblockgeneric.gifts.GiftDropRarity.COMMON;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftDropRarity.RARE;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftDropRarity.SANTA;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftDropRarity.SWEET;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftReward.coins;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftReward.item;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftReward.xp;
import static dev.daweapon.type.skyblockgeneric.gifts.GiftReward.xpBoost;

/**
 * The gifts players hand to each other, with the wiki's drop weights (White 6,649, Green 6,503,
 * Red 3,876) and North Star odds. The opener's roll decides the reward both players get.
 */
@Getter
public enum Gift {
    WHITE(ItemType.WHITE_GIFT, 0.10, 1, 1, 0, List.of(
            coins(COMMON, 2000, 2_000), coins(COMMON, 1000, 5_000), coins(RARE, 500, 10_000),
            xp(COMMON, 1500, 250), xp(COMMON, 200, 500),
            xpBoost(COMMON, 500, 1), xpBoost(COMMON, 250, 2),
            item(RARE, 400, ItemType.ICE_RUNE),
            item(SWEET, 60, ItemType.BATTLE_DISC), item(SWEET, 60, ItemType.WINTER_DISC),
            item(SWEET, 30, ItemType.SNOW_MINION),
            item(SWEET, 25, ItemType.GRINCH_PERSONALITY), item(SWEET, 25, ItemType.GINGERBREAD_PERSONALITY),
            item(SWEET, 25, ItemType.SANTA_PERSONALITY),
            item(SWEET, 20, ItemType.NUTCRACKER), item(SWEET, 20, ItemType.PRESENTS), item(SWEET, 20, ItemType.CHRISTMAS_TREE),
            item(SANTA, 4, ItemType.GIFT_THE_FISH),
            item(SWEET, 2, ItemType.SNOW_SUIT_HELMET), item(SWEET, 3, ItemType.SNOW_SUIT_CHESTPLATE),
            item(SWEET, 3, ItemType.SNOW_SUIT_LEGGINGS), item(SWEET, 2, ItemType.SNOW_SUIT_BOOTS))),
    GREEN(ItemType.GREEN_GIFT, 0.30, 1, 3, 0, List.of(
            coins(COMMON, 2000, 5_000), coins(COMMON, 1000, 10_000), coins(RARE, 20, 20_000),
            xp(COMMON, 1500, 250), xp(COMMON, 500, 500), xp(COMMON, 1, 2_500),
            xpBoost(COMMON, 125, 1), xpBoost(COMMON, 500, 2), xpBoost(RARE, 125, 3),
            item(RARE, 400, ItemType.ICE_RUNE),
            item(SWEET, 60, ItemType.BATTLE_DISC), item(SWEET, 60, ItemType.WINTER_DISC),
            item(SWEET, 50, ItemType.SNOW_MINION),
            item(SWEET, 25, ItemType.GINGERBREAD_PERSONALITY), item(SWEET, 25, ItemType.GRINCH_PERSONALITY),
            item(SWEET, 25, ItemType.SANTA_PERSONALITY),
            item(SWEET, 20, ItemType.NUTCRACKER), item(SWEET, 20, ItemType.PRESENTS), item(SWEET, 20, ItemType.CHRISTMAS_TREE),
            item(SWEET, 4, ItemType.SNOW_SUIT_HELMET), item(SWEET, 6, ItemType.SNOW_SUIT_CHESTPLATE),
            item(SWEET, 6, ItemType.SNOW_SUIT_LEGGINGS), item(SWEET, 4, ItemType.SNOW_SUIT_BOOTS),
            item(SANTA, 5, ItemType.GIFT_THE_FISH), item(SANTA, 2, ItemType.GOLD_GIFT))),
    RED(ItemType.RED_GIFT, 0.50, 1, 5, 0.0125, List.of(
            coins(RARE, 400, 5_000), coins(RARE, 750, 20_000), coins(SWEET, 20, 100_000), coins(SANTA, 1, 1_000_000),
            xp(COMMON, 100, 250), xp(COMMON, 1000, 500), xp(COMMON, 5, 2_500),
            xpBoost(COMMON, 125, 2), xpBoost(RARE, 500, 3),
            item(RARE, 400, ItemType.ICE_RUNE),
            item(SWEET, 70, ItemType.LARGE_WINTER_SACK), item(SWEET, 65, ItemType.SNOW_MINION),
            item(SWEET, 60, ItemType.NEW_BOTTLE_OF_JYRRE),
            item(SWEET, 60, ItemType.BATTLE_DISC), item(SWEET, 60, ItemType.WINTER_DISC),
            item(SWEET, 25, ItemType.GINGERBREAD_PERSONALITY), item(SWEET, 25, ItemType.GRINCH_PERSONALITY),
            item(SWEET, 25, ItemType.SANTA_PERSONALITY),
            item(SWEET, 25, ItemType.SNOW_SUIT_HELMET), item(SWEET, 25, ItemType.SNOW_SUIT_CHESTPLATE),
            item(SWEET, 25, ItemType.SNOW_SUIT_LEGGINGS), item(SWEET, 25, ItemType.SNOW_SUIT_BOOTS),
            item(SWEET, 20, ItemType.NUTCRACKER), item(SWEET, 20, ItemType.PRESENTS), item(SWEET, 20, ItemType.CHRISTMAS_TREE),
            item(SANTA, 6, ItemType.GIFT_THE_FISH), item(SANTA, 5, ItemType.GOLD_GIFT),
            item(SANTA, 4, ItemType.KRAMPUS_HELMET), item(SANTA, 4, ItemType.CRYOPOWDER_SHARD),
            item(SANTA, 4, ItemType.WINTER_ISLAND), item(SANTA, 2, ItemType.SNOWMAN_PET)));

    private final ItemType item;
    private final double northStarChance;
    private final int minNorthStars;
    private final int maxNorthStars;
    private final double hollyDyeChancePercent;
    private final List<GiftReward> rewards;

    Gift(ItemType item, double northStarChance, int minNorthStars, int maxNorthStars, double hollyDyeChancePercent,
         List<GiftReward> rewards) {
        this.item = item;
        this.northStarChance = northStarChance;
        this.minNorthStars = minNorthStars;
        this.maxNorthStars = maxNorthStars;
        this.hollyDyeChancePercent = hollyDyeChancePercent;
        this.rewards = rewards;
    }

    public static @Nullable Gift of(@Nullable ItemType type) {
        for (Gift gift : values()) {
            if (gift.item == type) return gift;
        }
        return null;
    }

    public int totalWeight() {
        return rewards.stream().mapToInt(GiftReward::weight).sum();
    }

    public GiftReward pick(@Nullable UUID opener, double rareItemMultiplier, RandomGenerator random) {
        return LootRoll.pick(opener, rewards, GiftReward::item, reward -> reward.weight(rareItemMultiplier), random);
    }

    public boolean rollHollyDye(@Nullable UUID opener, RandomGenerator random) {
        return hollyDyeChancePercent > 0 && LootRoll.roll(opener, ItemType.DYE_HOLLY, hollyDyeChancePercent, random);
    }

    public int rollNorthStars(RandomGenerator random) {
        if (random.nextDouble() >= northStarChance) return 0;
        return random.nextInt(minNorthStars, maxNorthStars + 1);
    }
}
