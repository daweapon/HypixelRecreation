package dev.daweapon.type.skyblockgeneric.garden.composter;

import java.util.function.DoublePredicate;

public final class ComposterEngine {
    private static final double SPEED_PER_TIER = 0.20;
    private static final double MULTI_DROP_PER_TIER = 3;
    private static final double COST_REDUCTION_PER_TIER = 0.01;
    private static final double CAP_PER_TIER = 30_000;

    private ComposterEngine() {
    }

    public static void initialise(ComposterState state, ComposterConfig config, long now) {
        if (state.isInitialised()) return;
        state.setInitialised(true);
        state.setOrganicMatter(config.tutorialMatter());
        state.setFuel(config.tutorialFuel());
        state.setLastUpdate(now);
    }

    public static double organicMatterCap(ComposterState state, ComposterConfig config) {
        return config.baseOrganicMatterCap() + organicMatterBonus(state.tier(ComposterUpgrade.ORGANIC_MATTER_CAP));
    }

    public static double organicMatterBonus(int tier) {
        double bonus = 0;
        for (int step = 1; step <= tier; step++) {
            bonus += step <= 8 ? 20_000 : step <= 17 ? 30_000 : 40_000;
        }
        return bonus;
    }

    public static double fuelCap(ComposterState state, ComposterConfig config) {
        return config.baseFuelCap() + CAP_PER_TIER * state.tier(ComposterUpgrade.FUEL_CAP);
    }

    public static double cycleSeconds(ComposterState state, ComposterConfig config) {
        if (!state.isFirstCycleDone()) return config.tutorialFirstCycleSeconds();
        return config.baseSeconds() / (1 + SPEED_PER_TIER * state.tier(ComposterUpgrade.SPEED));
    }

    public static double matterPerCycle(ComposterState state, ComposterConfig config) {
        return config.matterPerCompost() * (1 - COST_REDUCTION_PER_TIER * state.tier(ComposterUpgrade.COST_REDUCTION));
    }

    public static double fuelPerCycle(ComposterState state, ComposterConfig config) {
        if (!state.isFirstCycleDone()) return config.tutorialFirstCycleFuel();
        return config.fuelPerCompost() * (1 - COST_REDUCTION_PER_TIER * state.tier(ComposterUpgrade.COST_REDUCTION));
    }

    public static double multiDropChance(ComposterState state) {
        return MULTI_DROP_PER_TIER * state.tier(ComposterUpgrade.MULTI_DROP);
    }

    public static boolean canStart(ComposterState state, ComposterConfig config) {
        return state.getOrganicMatter() >= matterPerCycle(state, config) && state.getFuel() >= fuelPerCycle(state, config);
    }

    /** Advances the machine to {@code now}; returns the number of compost produced. {@code extraDrop} rolls a percent chance. */
    public static int advance(ComposterState state, ComposterConfig config, long now, DoublePredicate extraDrop) {
        if (state.getLastUpdate() == 0) {
            state.setLastUpdate(now);
            return 0;
        }
        double elapsed = Math.max(0, (now - state.getLastUpdate()) / 1000D);
        state.setLastUpdate(now);
        int produced = 0;
        while (true) {
            if (!state.running()) {
                if (!canStart(state, config)) break;
                state.setOrganicMatter(state.getOrganicMatter() - matterPerCycle(state, config));
                state.setFuel(state.getFuel() - fuelPerCycle(state, config));
                state.setRemainingSeconds(cycleSeconds(state, config));
            }
            if (elapsed <= 0) break;
            double step = Math.min(elapsed, state.getRemainingSeconds());
            state.setRemainingSeconds(state.getRemainingSeconds() - step);
            elapsed -= step;
            if (state.getRemainingSeconds() > 1e-9) break;
            state.setRemainingSeconds(0);
            state.setFirstCycleDone(true);
            produced += 1;
            if (extraDrop.test(multiDropChance(state))) produced += 1;
        }
        state.setStoredCompost(state.getStoredCompost() + produced);
        return produced;
    }
}
