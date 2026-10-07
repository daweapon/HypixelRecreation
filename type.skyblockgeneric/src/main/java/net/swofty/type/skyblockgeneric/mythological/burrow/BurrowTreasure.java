package net.swofty.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.random.RandomGenerator;

/**
 * The Ancestral Spade's treasure table from the SkyBlock Wiki (total weight 96), plus Myth the Fish,
 * which is rolled on its own at 0.01% per treasure.
 */
@Getter
public enum BurrowTreasure {
    GRIFFIN_FEATHER(64, ItemType.GRIFFIN_FEATHER, 0),
    COINS_2_500(15, null, 2_500),
    COINS_5_000(10, null, 5_000),
    COINS_10_000(5, null, 10_000),
    COINS_25_000(2, null, 25_000),
    MYTH_THE_FISH(0, ItemType.MYTH_THE_FISH, 0);

    private static final double MYTH_THE_FISH_CHANCE = 0.0001;

    private final int weight;
    private final @Nullable ItemType item;
    private final int coins;

    BurrowTreasure(int weight, @Nullable ItemType item, int coins) {
        this.weight = weight;
        this.item = item;
        this.coins = coins;
    }

    public static BurrowTreasure roll(RandomGenerator random) {
        if (random.nextDouble() < MYTH_THE_FISH_CHANCE) return MYTH_THE_FISH;
        int total = Arrays.stream(values()).mapToInt(BurrowTreasure::getWeight).sum();
        int pick = random.nextInt(total);
        for (BurrowTreasure treasure : values()) {
            pick -= treasure.weight;
            if (pick < 0) return treasure;
        }
        return GRIFFIN_FEATHER;
    }
}
