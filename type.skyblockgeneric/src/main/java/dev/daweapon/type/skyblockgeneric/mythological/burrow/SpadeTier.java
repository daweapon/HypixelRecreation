package dev.daweapon.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout;
import dev.daweapon.type.skyblockgeneric.reforge.abilities.EruditeAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public static final int ERUDITE_BONUS_LENGTH = EruditeAbility.EXTRA_BURROWS;
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
        return tier.chainLength + ReforgeLoadout.extraBurrows(spade);
    }

    public BurrowTreasure roll(@Nullable UUID player, RandomGenerator random) {
        if (LootRoll.roll(player, ItemType.DYE_MYTHOLOGICAL, MYTHOLOGICAL_DYE_CHANCE * 100, random)) {
            return BurrowTreasure.MYTHOLOGICAL_DYE;
        }
        if (LootRoll.roll(player, ItemType.MYTH_THE_FISH, MYTH_THE_FISH_CHANCE * 100, random)) {
            return BurrowTreasure.MYTH_THE_FISH;
        }
        return LootRoll.pick(player, treasures, BurrowTreasure::item, BurrowTreasure::weight, random);
    }
}
