package net.swofty.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * The three Griffin Burrow spades. Each sets the base chain length and its own treasure table
 * (weights from the SkyBlock Wiki's Griffin Burrows item loot tables).
 */
@Getter
public enum SpadeTier {
    ANCESTRAL(ItemType.ANCESTRAL_SPADE, 4, List.of(
            BurrowTreasure.item(64, ItemType.GRIFFIN_FEATHER),
            BurrowTreasure.coins(15, 2_500),
            BurrowTreasure.coins(10, 5_000),
            BurrowTreasure.coins(5, 10_000),
            BurrowTreasure.coins(2, 25_000))),
    ARCHAIC(ItemType.ARCHAIC_SPADE, 6, List.of(
            BurrowTreasure.item(375, ItemType.GRIFFIN_FEATHER),
            BurrowTreasure.coins(100, 5_000),
            BurrowTreasure.coins(40, 10_000),
            BurrowTreasure.item(40, ItemType.MYTHOS_FRAGMENT),
            BurrowTreasure.coins(20, 25_000),
            BurrowTreasure.coins(10, 50_000),
            BurrowTreasure.coins(5, 100_000),
            BurrowTreasure.coins(2, 250_000))),
    DEIFIC(ItemType.DEIFIC_SPADE, 8, List.of(
            BurrowTreasure.item(2_020, ItemType.GRIFFIN_FEATHER),
            BurrowTreasure.coins(500, 10_000),
            BurrowTreasure.item(300, ItemType.MYTHOS_FRAGMENT),
            BurrowTreasure.coins(200, 25_000),
            BurrowTreasure.coins(100, 50_000),
            BurrowTreasure.coins(50, 100_000),
            BurrowTreasure.coins(25, 250_000),
            BurrowTreasure.coins(15, 500_000),
            BurrowTreasure.coins(10, 1_000_000),
            BurrowTreasure.item(1, ItemType.BRAIDED_GRIFFIN_FEATHER)));

    public static final String ERUDITE = "erudite";
    public static final int ERUDITE_BONUS_LENGTH = 2;
    private static final double MYTH_THE_FISH_CHANCE = 0.0001;
    private static final double MYTHOLOGICAL_DYE_CHANCE = 0.000001;

    private final ItemType item;
    private final int chainLength;
    private final List<BurrowTreasure> treasures;

    SpadeTier(ItemType item, int chainLength, List<BurrowTreasure> treasures) {
        this.item = item;
        this.chainLength = chainLength;
        this.treasures = treasures;
    }

    public static Optional<SpadeTier> of(ItemType type) {
        return Arrays.stream(values()).filter(tier -> tier.item == type).findFirst();
    }

    public static Optional<SpadeTier> held(SkyBlockPlayer player) {
        return of(new SkyBlockItem(player.getItemInMainHand()).getAttributeHandler().getPotentialType());
    }

    public static int chainLength(SkyBlockItem spade) {
        SpadeTier tier = of(spade.getAttributeHandler().getPotentialType()).orElse(ANCESTRAL);
        Reforge reforge = spade.getAttributeHandler().getReforge();
        boolean erudite = reforge != null && reforge.getId().equalsIgnoreCase(ERUDITE);
        return tier.chainLength + (erudite ? ERUDITE_BONUS_LENGTH : 0);
    }

    public BurrowTreasure roll(RandomGenerator random) {
        if (random.nextDouble() < MYTHOLOGICAL_DYE_CHANCE) return BurrowTreasure.MYTHOLOGICAL_DYE;
        if (random.nextDouble() < MYTH_THE_FISH_CHANCE) return BurrowTreasure.MYTH_THE_FISH;
        int total = treasures.stream().mapToInt(BurrowTreasure::weight).sum();
        int pick = random.nextInt(total);
        for (BurrowTreasure treasure : treasures) {
            pick -= treasure.weight();
            if (pick < 0) return treasure;
        }
        return treasures.getFirst();
    }
}
