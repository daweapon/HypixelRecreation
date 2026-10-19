package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.region.IslandType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitRollerTest {
    private static RabbitRoller.Context context(int level, int shrine, boolean napoleon, IslandType island, Set<ChocolateRabbit> found, int mythicPity, int divinePity) {
        return new RabbitRoller.Context(null, level, shrine, napoleon, 0, 0, 0, 0, island, found, mythicPity, divinePity, 1, List.of());
    }

    @Test
    void baseWeightsMatchTheWiki() {
        RabbitRoller.Context context = context(6, 0, false, IslandType.HUB, Set.of(), 0, 0);
        double[] expected = {608, 250, 100, 30, 10, 4, 1};
        for (RabbitRarity rarity : RabbitRarity.values()) {
            assertEquals(expected[rarity.ordinal()], RabbitRoller.rarityWeight(context, rarity), 1e-9, rarity.name());
        }
    }

    @Test
    void allBuffsGiveTheWikiMaximumWeights() {
        RabbitRoller.Context context = new RabbitRoller.Context(null, 6, 20, true, 10, 10, 10, 0, IslandType.HUB, Set.of(), 0, 0, 1, List.of());
        double[] expected = {608, 350, 150, 45, 16, 6.4, 1.6};
        for (RabbitRarity rarity : RabbitRarity.values()) {
            assertEquals(expected[rarity.ordinal()], RabbitRoller.rarityWeight(context, rarity), 1e-9, rarity.name());
        }
    }

    @Test
    void factoryLevelLimitsTheRarities() {
        assertEquals(List.of(RabbitRarity.COMMON, RabbitRarity.UNCOMMON), RabbitRoller.availableRarities(1));
        assertEquals(7, RabbitRoller.availableRarities(6).size());
        Random random = new Random(7);
        for (int i = 0; i < 2_000; i++) {
            RabbitRoller.Result result = RabbitRoller.roll(context(1, 0, false, IslandType.HUB, Set.of(), 0, 0), random);
            assertTrue(result.rabbit().getRarity().ordinal() <= RabbitRarity.UNCOMMON.ordinal());
        }
    }

    @Test
    void observedRarityShareFollowsTheWeights() {
        Random random = new Random(42);
        Map<RabbitRarity, Integer> counts = new EnumMap<>(RabbitRarity.class);
        int rolls = 60_000;
        int mythicPity = 0;
        int divinePity = 0;
        for (int i = 0; i < rolls; i++) {
            RabbitRoller.Result result = RabbitRoller.roll(context(6, 0, false, IslandType.HUB, Set.of(), mythicPity, divinePity), random);
            mythicPity = result.mythicPity();
            divinePity = result.divinePity();
            counts.merge(result.rabbit().getRarity(), 1, Integer::sum);
        }
        assertEquals(0.6062, counts.get(RabbitRarity.COMMON) / (double) rolls, 0.015);
        assertEquals(0.2493, counts.get(RabbitRarity.UNCOMMON) / (double) rolls, 0.015);
        assertEquals(0.0997, counts.get(RabbitRarity.RARE) / (double) rolls, 0.01);
        assertTrue(counts.get(RabbitRarity.MYTHIC) / (double) rolls >= 0.004);
    }

    @Test
    void mythicPityGuaranteesAMythicEveryTwoHundredFiftyRabbits() {
        RabbitRoller.Result result = RabbitRoller.roll(context(6, 0, false, IslandType.HUB, Set.of(), RabbitRoller.MYTHIC_PITY - 1, 0), new Random(1));
        assertEquals(RabbitRarity.MYTHIC, result.rabbit().getRarity());
        assertEquals(0, result.mythicPity());
        RabbitRoller.Result early = RabbitRoller.roll(context(6, 0, false, IslandType.HUB, Set.of(), 10, 0), new Random(1));
        assertTrue(early.mythicPity() == 11 || early.mythicPity() == 0);
    }

    @Test
    void divinePityGuaranteesADivineEveryTwelveHundredFiftyRabbits() {
        RabbitRoller.Result result = RabbitRoller.roll(context(6, 0, false, IslandType.HUB, Set.of(), 0, RabbitRoller.DIVINE_PITY - 1), new Random(1));
        assertEquals(RabbitRarity.DIVINE, result.rabbit().getRarity());
        assertEquals(0, result.divinePity());
    }

    @Test
    void pityDoesNotApplyBeforeTheRarityIsUnlocked() {
        RabbitRoller.Result result = RabbitRoller.roll(context(2, 0, false, IslandType.HUB, Set.of(), 5_000, 5_000), new Random(3));
        assertTrue(result.rabbit().getRarity().ordinal() <= RabbitRarity.RARE.ordinal());
    }

    @Test
    void residentRabbitsOnlyAppearOnTheirOwnIsland() {
        Random random = new Random(9);
        for (int i = 0; i < 5_000; i++) {
            RabbitRoller.Result result = RabbitRoller.roll(context(6, 0, false, IslandType.GOLD_MINE, Set.of(), 0, 0), random);
            IslandType home = result.rabbit().getLocation();
            assertTrue(home == null || home == IslandType.GOLD_MINE, result.rabbit().name());
        }
    }

    @Test
    void theHitmanCanFindResidentsFromAnyIsland() {
        Random random = new Random(11);
        Set<IslandType> homes = new HashSet<>();
        for (int i = 0; i < 20_000; i++) {
            RabbitRoller.Result result = RabbitRoller.roll(context(2, 0, false, null, Set.of(), 0, 0), random);
            if (result.rabbit().getLocation() != null) homes.add(result.rabbit().getLocation());
        }
        assertTrue(homes.size() > 4);
    }

    @Test
    void rabbitsWithRequirementsAreNeverRolled() {
        Random random = new Random(5);
        for (int i = 0; i < 20_000; i++) {
            RabbitRoller.Result result = RabbitRoller.roll(context(6, 0, false, IslandType.HUB, Set.of(), 0, 0), random);
            assertTrue(result.rabbit().isEggRollable(), result.rabbit().name());
        }
        assertFalse(ChocolateRabbit.HUBBY.isEggRollable());
        assertTrue(ChocolateRabbit.HUBBY.isUniqueLocationRabbit());
        assertFalse(ChocolateRabbit.ABI.isEggRollable());
    }

    @Test
    void duplicatesAreFlaggedAndUnobtainedBonusFavoursNewRabbits() {
        Set<ChocolateRabbit> found = new HashSet<>(List.of(ChocolateRabbit.AARON));
        RabbitRoller.Context plain = context(1, 0, false, IslandType.HUB, found, 0, 0);
        RabbitRoller.Context bonus = new RabbitRoller.Context(null, 1, 0, false, 0, 0, 0, 50, IslandType.HUB, found, 0, 0, 1, List.of());
        assertTrue(RabbitRoller.rabbitWeight(bonus, ChocolateRabbit.ABLE) > RabbitRoller.rabbitWeight(plain, ChocolateRabbit.ABLE));
        assertEquals(RabbitRoller.rabbitWeight(plain, ChocolateRabbit.AARON), RabbitRoller.rabbitWeight(bonus, ChocolateRabbit.AARON), 1e-9);
        assertNotNull(RabbitRoller.roll(plain, new Random(1)));
    }

    @Test
    void hotspotsAreStablePerYearAndOnlyForNonResidents() {
        List<IslandType> islands = List.of(IslandType.HUB, IslandType.GOLD_MINE, IslandType.THE_END);
        int hotspots = 0;
        for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
            IslandType first = RabbitHotspots.islandFor(1, rabbit, islands);
            assertEquals(first, RabbitHotspots.islandFor(1, rabbit, islands));
            if (rabbit.getLocation() != null) assertNull(first);
            if (first != null) hotspots++;
        }
        assertTrue(hotspots > 20);
    }
}
