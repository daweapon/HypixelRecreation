package dev.daweapon.type.island.lifecycle;

import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecyclePhase;
import dev.daweapon.type.skyblockgeneric.user.island.IslandLifecycleStep;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IslandLifecycleStepsTest {
    private static final Comparator<IslandLifecycleStep> RUN_ORDER =
            Comparator.comparing(IslandLifecycleStep::phase).thenComparingInt(IslandLifecycleStep::order);

    private static List<IslandLifecycleStep> steps() {
        return List.of(new JerryDefaultsStep(), new StarterChestStep(), new FirstMinionStep(),
                new JerryLoadStep(), new MinionLoadStep(), new PortalBuildStep(),
                new MinionSaveStep(), new JerrySaveStep());
    }

    @Test
    void stepsAreAssignedToTheExpectedPhases() {
        assertEquals(IslandLifecyclePhase.CREATE, new StarterChestStep().phase());
        assertEquals(IslandLifecyclePhase.CREATE, new FirstMinionStep().phase());
        assertEquals(IslandLifecyclePhase.LOAD, new PortalBuildStep().phase());
        assertEquals(IslandLifecyclePhase.SAVE, new JerrySaveStep().phase());
    }

    @Test
    void ordersAreUniqueWithinEachPhase() {
        for (IslandLifecyclePhase phase : IslandLifecyclePhase.values()) {
            Set<Integer> orders = new HashSet<>();
            for (IslandLifecycleStep step : steps()) {
                if (step.phase() == phase) {
                    assertTrue(orders.add(step.order()), step.getClass().getSimpleName() + " shares an order in " + phase);
                }
            }
        }
    }

    @Test
    void starterChestIsPlacedBeforeFirstMinion() {
        assertTrue(new StarterChestStep().order() < new FirstMinionStep().order());
    }

    @Test
    void minionsLoadBeforePortalIsBuilt() {
        assertTrue(new MinionLoadStep().order() < new PortalBuildStep().order());
    }

    @Test
    void sortingByRunOrderGroupsPhasesTogether() {
        List<IslandLifecycleStep> sorted = steps().stream().sorted(RUN_ORDER).toList();
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i - 1).phase().compareTo(sorted.get(i).phase()) <= 0);
        }
    }
}
