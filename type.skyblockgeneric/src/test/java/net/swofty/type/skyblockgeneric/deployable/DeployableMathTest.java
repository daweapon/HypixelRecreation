package net.swofty.type.skyblockgeneric.deployable;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeployableMathTest {
    @Test
    void rangeIsInclusive() {
        assertTrue(DeployableMath.inRange(18, 18));
        assertFalse(DeployableMath.inRange(18.01, 18));
    }

    @Test
    void recipientsAreTheClosestWithinRangeUpToTheCap() {
        List<DeployableMath.Candidate> candidates = List.of(
                new DeployableMath.Candidate("a", 17), new DeployableMath.Candidate("b", 3),
                new DeployableMath.Candidate("c", 25), new DeployableMath.Candidate("d", 9),
                new DeployableMath.Candidate("e", 1), new DeployableMath.Candidate("f", 5),
                new DeployableMath.Candidate("g", 12));
        assertEquals(List.of("e", "b", "f", "d", "g"), DeployableMath.recipients(candidates, 18, 5));
        assertEquals(List.of("e", "b"), DeployableMath.recipients(candidates, 18, 2));
    }

    @Test
    void highestRankWinsThenOldestThenClosest() {
        assertEquals(1, DeployableMath.strongest(List.of(
                new DeployableMath.Buff(10, 0, 1), new DeployableMath.Buff(40, 5, 9))));
        assertEquals(0, DeployableMath.strongest(List.of(
                new DeployableMath.Buff(20, 1, 9), new DeployableMath.Buff(20, 2, 1))));
        assertEquals(1, DeployableMath.strongest(List.of(
                new DeployableMath.Buff(20, 1, 9), new DeployableMath.Buff(20, 1, 1))));
        assertEquals(-1, DeployableMath.strongest(List.of()));
    }

    @Test
    void durationsAndOrbValues() {
        assertEquals(60, DeployableMath.durationSeconds(DeployableType.RADIANT_POWER_ORB.durationSeconds(), false));
        assertEquals(120, DeployableMath.durationSeconds(DeployableType.RADIANT_POWER_ORB.durationSeconds(), true));
        assertEquals(300, DeployableType.MITHRIL_LANTERN.durationSeconds());
        assertEquals(20, DeployableType.PLASMAFLUX_POWER_ORB.radius());
        assertEquals(30, DeployableType.PLASMAFLUX_POWER_ORB.otherHealPerSecond());
        assertEquals(DeployableType.RADIANT_POWER_ORB, DeployableType.ofItem("RADIANT_POWER_ORB"));
    }

    @Test
    void healthRegenScalesWithTheStat() {
        assertEquals(3.5F, DeployableMath.regenPerPulse(200, 100, 0), 1e-4);
        assertEquals(7F, DeployableMath.regenPerPulse(200, 200, 0), 1e-4);
        assertEquals(3.5F * 1.25F, DeployableMath.regenPerPulse(200, 125, 0), 1e-4);
        assertEquals(3.5F * 2.25F, DeployableMath.regenPerPulse(200, 225, 0), 1e-4);
    }

    @Test
    void manaRegenAndCost() {
        assertEquals(10, DeployableMath.manaPerSecond(500, 0));
        assertEquals(20, DeployableMath.manaPerSecond(500, 1.0));
        assertEquals(22, DeployableMath.manaPerSecond(500, 1.25));
        assertEquals(250, DeployableMath.manaCost(500, DeployableType.MANA_COST_FRACTION));
    }

    @Test
    void umberellaDoublesDuringWeather() {
        assertEquals(5, DeployableType.UMBERELLA.statistics(false).getBase(ItemStatistic.TROPHY_FISH_CHANCE));
        assertEquals(10, DeployableType.UMBERELLA.statistics(true).getBase(ItemStatistic.TROPHY_FISH_CHANCE));
        assertEquals(35, DeployableType.PLASMAFLUX_POWER_ORB.statistics(true).getBase(ItemStatistic.STRENGTH));
    }
}
