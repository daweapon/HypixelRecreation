package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HoppityShopStockTest {
    private static final UUID PLAYER = UUID.randomUUID();

    @Test
    void stockHasSevenDistinctEggRollableRabbits() {
        Random random = new Random(11);
        for (int i = 0; i < 500; i++) {
            List<ChocolateRabbit> stock = HoppityShopStock.generate(PLAYER, random);
            assertEquals(HoppityShopStock.SLOTS, stock.size());
            assertEquals(7, new HashSet<>(stock).size());
            stock.forEach(rabbit -> assertTrue(rabbit.isEggRollable(), rabbit.name()));
        }
    }

    @Test
    void firstTwoSlotsAreAlwaysCommon() {
        Random random = new Random(5);
        for (int i = 0; i < 500; i++) {
            List<ChocolateRabbit> stock = HoppityShopStock.generate(PLAYER, random);
            assertEquals(RabbitRarity.COMMON, stock.get(0).getRarity());
            assertEquals(RabbitRarity.COMMON, stock.get(1).getRarity());
        }
    }

    @Test
    void slotsNeverExceedTheirHighestRarity() {
        Random random = new Random(9);
        int[] highest = {0, 0, 1, 1, 2, 3, 4};
        for (int i = 0; i < 2_000; i++) {
            List<ChocolateRabbit> stock = HoppityShopStock.generate(PLAYER, random);
            for (int slot = 0; slot < stock.size(); slot++) {
                assertTrue(stock.get(slot).getRarity().ordinal() <= highest[slot], "slot " + slot);
            }
        }
    }

    @Test
    void thirdSlotDrawsUncommonAboutTenPercent() {
        Random random = new Random(42);
        int uncommon = 0;
        for (int i = 0; i < 10_000; i++) {
            if (HoppityShopStock.rollRarity(PLAYER, 2, random) == RabbitRarity.UNCOMMON) uncommon++;
        }
        assertEquals(1_000, uncommon, 150);
    }

    @Test
    void seventhSlotDistributionMatchesTheWiki() {
        Random random = new Random(3);
        int[] counts = new int[5];
        for (int i = 0; i < 20_000; i++) counts[HoppityShopStock.rollRarity(PLAYER, 6, random).ordinal()]++;
        double[] expected = {10, 45, 35, 7, 3};
        for (int i = 0; i < 5; i++) assertEquals(expected[i], counts[i] / 200.0, 1.5, "rarity " + i);
    }

    @Test
    void pricesMatchTheWiki() {
        assertEquals(200_000L, HoppityShopStock.price(RabbitRarity.COMMON));
        assertEquals(500_000L, HoppityShopStock.price(RabbitRarity.UNCOMMON));
        assertEquals(1_000_000L, HoppityShopStock.price(RabbitRarity.RARE));
        assertEquals(2_500_000L, HoppityShopStock.price(RabbitRarity.EPIC));
        assertEquals(5_000_000L, HoppityShopStock.price(RabbitRarity.LEGENDARY));
    }

    @Test
    void everyShopRarityHasEnoughRabbitsForOneStock() {
        for (RabbitRarity rarity : List.of(RabbitRarity.COMMON, RabbitRarity.UNCOMMON)) {
            assertTrue(HoppityShopStock.candidates(rarity, Set.of()).size() >= 2, rarity.name());
        }
    }

    @Test
    void offerBeforeOwningAbiMatchesTheTable() {
        List<HoppityAbiphoneOffer.Entry> entries = HoppityAbiphoneOffer.entries(false);
        assertEquals(5, entries.size());
        assertEquals(ChocolateRabbit.ABI, entries.getFirst().fixed());
        assertEquals(500_000L, entries.getFirst().price());
        assertEquals(105, entries.stream().mapToDouble(HoppityAbiphoneOffer.Entry::weight).sum(), 1e-9);
    }

    @Test
    void offerAfterOwningAbiNeverOffersAbiAndMatchesPercentages() {
        List<HoppityAbiphoneOffer.Entry> entries = HoppityAbiphoneOffer.entries(true);
        double[] expected = {50.0, 14.29, 28.57, 7.14};
        for (int i = 0; i < entries.size(); i++) {
            assertEquals(expected[i], HoppityAbiphoneOffer.chancePercent(true, entries.get(i)), 0.01);
        }
        Random random = new Random(8);
        for (int i = 0; i < 2_000; i++) {
            HoppityAbiphoneOffer.Offer offer = HoppityAbiphoneOffer.roll(PLAYER, true, random);
            assertNotNull(offer.rabbit());
            assertFalse(offer.rabbit() == ChocolateRabbit.ABI);
        }
    }

    @Test
    void offerBeforeOwningAbiSometimesOffersAbi() {
        Random random = new Random(21);
        int abi = 0;
        for (int i = 0; i < 10_000; i++) {
            HoppityAbiphoneOffer.Offer offer = HoppityAbiphoneOffer.roll(PLAYER, false, random);
            if (offer.rabbit() == ChocolateRabbit.ABI) {
                abi++;
                assertEquals(500_000L, offer.price());
            }
        }
        assertEquals(10_000 * 35.0 / 105.0, abi, 250);
    }
}
