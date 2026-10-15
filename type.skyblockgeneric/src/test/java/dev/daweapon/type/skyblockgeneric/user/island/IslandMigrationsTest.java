package dev.daweapon.type.skyblockgeneric.user.island;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IslandMigrationsTest {

    @Test
    void theStartingVersionNeedsNoSteps() {
        assertDoesNotThrow(() -> IslandMigrations.validate(Map.of(), 1));
    }

    @Test
    void bumpingTheVersionWithoutAStepIsRefused() {
        assertThrows(IllegalStateException.class, () -> IslandMigrations.validate(Map.of(), 2));
        assertThrows(IllegalStateException.class,
                () -> IslandMigrations.validate(Map.of(1, (world, island) -> world), 3));
    }

    @Test
    void stepsRunInOrderFromTheStoredVersion() {
        List<Integer> ran = new ArrayList<>();
        Map<Integer, IslandMigration> steps = Map.of(
                1, (world, island) -> { ran.add(1); return world; },
                2, (world, island) -> { ran.add(2); return world; },
                3, (world, island) -> { ran.add(3); return world; });

        IslandMigrations.apply(steps, null, null, 2, 4);

        assertEquals(List.of(2, 3), ran);
    }

    @Test
    void aMissingStepFailsTheLoadRatherThanSkippingIt() {
        assertThrows(IllegalStateException.class,
                () -> IslandMigrations.apply(Map.of(), null, null, 1, 2));
    }
}
