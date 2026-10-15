package dev.daweapon.type.skyblockgeneric.gifts;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JerryEffectsTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void jyrreGainsOneIntelligencePerHourUpToThreeHundred() {
        assertEquals(0, BottleOfJyrre.bonusFor(0));
        assertEquals(0, BottleOfJyrre.bonusFor(59));
        assertEquals(1, BottleOfJyrre.bonusFor(60));
        assertEquals(150, BottleOfJyrre.bonusFor(150 * 60 + 30));
        assertEquals(300, BottleOfJyrre.bonusFor(300 * 60));
        assertEquals(300, BottleOfJyrre.bonusFor(10_000 * 60));
        assertEquals(0, BottleOfJyrre.bonusFor(-5));
    }

    @Test
    void jyrreEvolvesAtTheCapAfterThreeHundredHours() {
        assertFalse(BottleOfJyrre.hasEvolved(300 * 60 - 1));
        assertTrue(BottleOfJyrre.hasEvolved(300 * 60));
        assertEquals(10, BottleOfJyrre.REFINED_LIMIT * BottleOfJyrre.REFINED_XP_EACH);
    }

    @Test
    void snowmanDamageReductionAndSnowCannonCap() {
        assertEquals(90, SnowmanPet.reduced(100, 10), 1e-9);
        assertEquals(0, SnowmanPet.reduced(100, 150), 1e-9);
        assertEquals(20_000, SnowmanPet.snowCannonDamage(100_000, 20), 1e-9);
        assertEquals(200_000, SnowmanPet.snowCannonDamage(5_000_000, 20), 1e-9);
        assertEquals(0, SnowmanPet.snowCannonDamage(-50, 20), 1e-9);
    }

    @Test
    void snowSuitCountsEachDistinctPieceIncludingEquipment() {
        assertEquals(0, SnowSuit.countPieces(List.of()));
        assertEquals(2, SnowSuit.countPieces(Arrays.asList(ItemType.SNOW_SUIT_HELMET, null, ItemType.SNOW_BELT, ItemType.DIRT)));
        assertEquals(SnowSuit.TOTAL_PIECES, SnowSuit.countPieces(List.of(
                ItemType.SNOW_SUIT_HELMET, ItemType.SNOW_SUIT_CHESTPLATE, ItemType.SNOW_SUIT_LEGGINGS, ItemType.SNOW_SUIT_BOOTS,
                ItemType.SNOW_NECKLACE, ItemType.SNOW_CLOAK, ItemType.SNOW_BELT, ItemType.SNOW_GLOVES)));
    }

    @Test
    void cannonsHoldTheirWikiCapacitiesAndRefillAtOneCoinPerSnowball() {
        assertEquals(1_000, SnowCannon.Tier.of(ItemType.SNOW_CANNON).capacity);
        assertEquals(1_000, SnowCannon.Tier.of(ItemType.SNOW_CANNON).startingAmmo);
        assertEquals(2_000, SnowCannon.Tier.of(ItemType.SNOW_BLASTER).capacity);
        assertEquals(0, SnowCannon.Tier.of(ItemType.SNOW_BLASTER).startingAmmo);
        assertEquals(10_000, SnowCannon.Tier.of(ItemType.SNOW_HOWITZER).capacity);
        assertNull(SnowCannon.Tier.of(ItemType.DIRT));

        assertEquals(41, SnowCannon.ammoAfterShot(42, false));
        assertEquals(42, SnowCannon.ammoAfterShot(42, true));
        assertEquals(0, SnowCannon.ammoAfterShot(0, false));

        assertEquals(10, SnowCannon.acceptedSnowballs(990, 1_000, 64));
        assertEquals(64, SnowCannon.acceptedSnowballs(0, 1_000, 64));
        assertEquals(0, SnowCannon.acceptedSnowballs(1_000, 1_000, 64));

        assertEquals(492, SnowCannon.affordableRefill(492, 10_000));
        assertEquals(300, SnowCannon.affordableRefill(492, 300.9));
        assertEquals(0, SnowCannon.affordableRefill(492, 0));
    }
}
