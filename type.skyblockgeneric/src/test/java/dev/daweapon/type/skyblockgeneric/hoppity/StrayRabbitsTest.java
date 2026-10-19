package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.hoppity.StrayRabbits.Golden;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrayRabbitsTest {
    private static final double[] BASE_WEIGHTS = {608, 250, 100, 30, 10, 4, 1};
    private static final java.util.function.ToDoubleFunction<RabbitRarity> WEIGHTS = rarity -> BASE_WEIGHTS[rarity.ordinal()];

    @Test
    void rewardsAreSecondsOfProductionAndDoubleDuringTheHunt() {
        int[] seconds = {5, 7, 10, 15, 20, 30, 50};
        for (RabbitRarity rarity : RabbitRarity.values()) {
            assertEquals(seconds[rarity.ordinal()], StrayRabbits.rewardSeconds(rarity, false));
            assertEquals(seconds[rarity.ordinal()] * 2, StrayRabbits.rewardSeconds(rarity, true));
        }
    }

    @Test
    void goldenBonusesOnlyAppearOnceTheirFactoryLevelIsReached() {
        Random random = new Random(3);
        for (int i = 0; i < 3_000; i++) {
            Golden golden = StrayRabbits.pickGolden(null, 1, random);
            assertTrue(golden == Golden.JACKPOT || golden == Golden.SIDE_DISH || golden == Golden.STAMPEDE, golden.name());
        }
        Map<Golden, Integer> counts = new EnumMap<>(Golden.class);
        for (int i = 0; i < 50_000; i++) counts.merge(StrayRabbits.pickGolden(null, 6, random), 1, Integer::sum);
        assertEquals(Golden.values().length, counts.size());
        assertEquals(35 / 95.0, counts.get(Golden.JACKPOT) / 50_000.0, 0.015);
        assertEquals(20 / 95.0, counts.get(Golden.SIDE_DISH) / 50_000.0, 0.015);
    }

    @Test
    void strayRabbitsOnlyComeFromTheFoundCollection() {
        Set<ChocolateRabbit> found = Set.of(ChocolateRabbit.AARON, ChocolateRabbit.DANTE);
        Random random = new Random(5);
        int mythic = 0;
        for (int i = 0; i < 5_000; i++) {
            ChocolateRabbit rabbit = StrayRabbits.pickRabbit(null, found, 6, WEIGHTS, random);
            assertTrue(found.contains(rabbit));
            if (rabbit == ChocolateRabbit.DANTE) mythic++;
        }
        assertTrue(mythic > 0 && mythic < 200);
        assertNull(StrayRabbits.pickRabbit(null, Set.of(), 6, WEIGHTS, random));
        assertEquals(ChocolateRabbit.AARON, StrayRabbits.pickRabbit(null, found, 1, WEIGHTS, random));
    }

    @Test
    void spawnAndGoldenChancesFollowTheWiki() {
        Random random = new Random(8);
        int spawns = 0;
        int golden = 0;
        int goldenOmega = 0;
        int fish = 0;
        int runs = 100_000;
        for (int i = 0; i < runs; i++) {
            if (StrayRabbits.spawnsThisCheck(null, random)) spawns++;
            if (StrayRabbits.isGolden(null, false, random)) golden++;
            if (StrayRabbits.isGolden(null, true, random)) goldenOmega++;
            if (StrayRabbits.isFish(null, random)) fish++;
        }
        assertEquals(0.20, spawns / (double) runs, 0.01);
        assertEquals(0.10, golden / (double) runs, 0.01);
        assertEquals(0.20, goldenOmega / (double) runs, 0.01);
        assertEquals(0.01, fish / (double) runs, 0.003);
        assertEquals(10, StrayRabbits.STAMPEDE_STRAYS);
    }
}
