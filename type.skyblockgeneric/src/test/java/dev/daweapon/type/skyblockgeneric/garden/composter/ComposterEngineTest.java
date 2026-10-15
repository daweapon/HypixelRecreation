package dev.daweapon.type.skyblockgeneric.garden.composter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComposterEngineTest {
    private static ComposterConfig config;

    @BeforeAll
    static void load() {
        ComposterConfig.load(Path.of("../configuration/skyblock/garden/composter.json"));
        config = ComposterConfig.get();
    }

    private ComposterState fresh() {
        ComposterState state = new ComposterState();
        ComposterEngine.initialise(state, config, 1_000);
        return state;
    }

    @Test
    void startsWithTheTutorialAmountsAndIdleUntilFuelArrives() {
        ComposterState state = fresh();
        assertEquals(4_000, state.getOrganicMatter());
        assertEquals(1_700, state.getFuel());
        assertEquals(0, ComposterEngine.advance(state, config, 600_000, chance -> false));
        assertFalse(state.running());
    }

    @Test
    void firstCycleTakesTenSecondsAndThreeThousandFuel() {
        ComposterState state = fresh();
        state.setFuel(4_700);
        assertEquals(1, ComposterEngine.advance(state, config, 1_000 + 10_000, chance -> false));
        assertEquals(1_700, state.getFuel());
        assertEquals(0, state.getOrganicMatter());
        assertEquals(1, state.getStoredCompost());
        assertTrue(state.isFirstCycleDone());
    }

    @Test
    void normalCycleUsesTheTierFormulas() {
        ComposterState state = fresh();
        state.setFirstCycleDone(true);
        state.setTier(ComposterUpgrade.SPEED, 5);
        state.setTier(ComposterUpgrade.COST_REDUCTION, 25);
        assertEquals(300, ComposterEngine.cycleSeconds(state, config), 1e-9);
        assertEquals(3_000, ComposterEngine.matterPerCycle(state, config), 1e-9);
        assertEquals(1_500, ComposterEngine.fuelPerCycle(state, config), 1e-9);
        assertEquals(850_000, ComposterEngine.fuelCap(fresh(), config) + 750_000, 1e-9);
    }

    @Test
    void offlineProductionStopsWhenMatterOrFuelRunOut() {
        ComposterState state = fresh();
        state.setFirstCycleDone(true);
        state.setOrganicMatter(40_000);
        state.setFuel(100_000);
        int produced = ComposterEngine.advance(state, config, 1_000 + 24L * 3_600_000, chance -> false);
        assertEquals(10, produced);
        assertEquals(0, state.getOrganicMatter(), 1e-9);
        assertEquals(80_000, state.getFuel(), 1e-9);
    }

    @Test
    void multiDropAddsExtraCompost() {
        ComposterState state = fresh();
        state.setFirstCycleDone(true);
        state.setTier(ComposterUpgrade.MULTI_DROP, 10);
        state.setOrganicMatter(8_000);
        state.setFuel(4_000);
        assertEquals(4, ComposterEngine.advance(state, config, 1_000 + 3_600_000L, chance -> chance == 30));
    }

    @Test
    void upgradeTablesMatchTheWiki() {
        assertEquals(100, config.tier(ComposterUpgrade.SPEED, 1).copper());
        assertEquals(4_000, config.tier(ComposterUpgrade.SPEED, 25).copper());
        int[] max = new int[ComposterUpgrade.values().length];
        java.util.Arrays.fill(max, ComposterUpgrade.MAX_TIER);
        assertEquals(305, config.skyblockXp(max));
    }

    @Test
    void stateRoundTrips() {
        ComposterState state = fresh();
        state.setTier(ComposterUpgrade.FUEL_CAP, 7);
        state.setStoredCompost(42);
        ComposterState copy = ComposterState.fromJson(state.toJson());
        assertEquals(7, copy.tier(ComposterUpgrade.FUEL_CAP));
        assertEquals(42, copy.getStoredCompost());
        assertEquals(4_000, copy.getOrganicMatter());
    }
}
