package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoFuelGrade;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoMinionFuelComponent;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoMinionProduction;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoMinionProduction.Drop;
import dev.daweapon.type.skyblockgeneric.minion.inferno.InfernoRareDrop;
import dev.daweapon.type.skyblockgeneric.minion.minions.combat.MinionInferno;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfernoMinionProductionTest {
    private static final RandomGenerator ALWAYS_WINS = () -> 0L;
    private static final RandomGenerator NEVER_WINS = () -> -1L;

    @Test
    void unfuelledMinionOnlyMakesCrudeGabagool() {
        assertEquals(List.of(new Drop(ItemType.CRUDE_GABAGOOL, 1)),
                InfernoMinionProduction.roll(null, 11, false, item -> null, ALWAYS_WINS));
    }

    @Test
    void specialtyReplacesGabagoolButOnlyHypergolicMakesUniqueItems() {
        InfernoMinionFuelComponent heavy = new InfernoMinionFuelComponent(InfernoFuelGrade.HEAVY, ItemType.NETHER_WART, 3, 7);
        assertEquals(List.of(new Drop(ItemType.NETHER_WART, 3)),
                InfernoMinionProduction.roll(heavy, 11, false, item -> null, ALWAYS_WINS));
        assertEquals(List.of(new Drop(ItemType.CRUDE_GABAGOOL, 1)),
                InfernoMinionProduction.roll(heavy, 11, false, item -> null, NEVER_WINS));
    }

    @Test
    void hypergolicRollsEveryUniqueItemAndApexMinionDoublesApexes() {
        InfernoMinionFuelComponent hypergolic = new InfernoMinionFuelComponent(InfernoFuelGrade.HYPERGOLIC, ItemType.CRUDE_GABAGOOL, 1, 1);
        List<Drop> tierNine = InfernoMinionProduction.roll(hypergolic, 9, false, item -> null, ALWAYS_WINS);
        List<Drop> tierTen = InfernoMinionProduction.roll(hypergolic, 10, false, item -> null, ALWAYS_WINS);

        assertEquals(1 + InfernoRareDrop.values().length, tierNine.size());
        assertTrue(tierNine.contains(new Drop(ItemType.INFERNO_APEX, 1)));
        assertTrue(tierTen.contains(new Drop(ItemType.INFERNO_APEX, 2)));
    }

    @Test
    void chancesMatchTheWikiAndEyedropsSkipChiliPeppers() {
        assertEquals(100.0 / 5_950, InfernoRareDrop.INFERNO_VERTEX.chancePercent(false), 1e-12);
        assertEquals(130.0 / 5_950, InfernoRareDrop.INFERNO_VERTEX.chancePercent(true), 1e-12);
        assertEquals(100.0 / 136, InfernoRareDrop.CHILI_PEPPER.chancePercent(true), 1e-12);
    }

    @Test
    void fuelAndRisingCelsiusSpeedUpTheMinion() {
        assertEquals(11, InfernoFuelGrade.FUEL.actionTimeDivisor());
        assertEquals(16, InfernoFuelGrade.HEAVY.actionTimeDivisor());
        assertEquals(21, InfernoFuelGrade.HYPERGOLIC.actionTimeDivisor());
        assertEquals(54, MinionInferno.risingCelsius(3));
        assertEquals(180, MinionInferno.risingCelsius(12));
    }

    @Test
    void elevenTiersWithTheWikiTimings() {
        List<SkyBlockMinion.MinionTier> tiers = new MinionInferno().getTiers();
        assertEquals(11, tiers.size());
        assertEquals(1013f, tiers.getFirst().timeBetweenActions());
        assertEquals(697f, tiers.getLast().timeBetweenActions());
    }
}
