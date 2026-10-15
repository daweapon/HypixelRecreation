package dev.daweapon.dungeons.catacombs.mob;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloorMobScalingTest {
    @Test
    void multiplierGrowsFivePercentPerRoomBeyondTheFirst() {
        assertEquals(1.0, FloorMobScaling.roomsExploredMultiplier(0), 1e-9);
        assertEquals(1.0, FloorMobScaling.roomsExploredMultiplier(1), 1e-9);
        assertEquals(1.05, FloorMobScaling.roomsExploredMultiplier(2), 1e-9);
        assertEquals(1.5, FloorMobScaling.roomsExploredMultiplier(11), 1e-9);
    }

    @Test
    void definitionScalesHealthAndDamage() {
        DungeonMobDefinition grunt = DungeonMobRegistry.defaults().mob("ZOMBIE_GRUNT");
        DungeonMobDefinition scaled = grunt.scaleForRoomsExplored(11);
        assertEquals(10_500, scaled.health());
        assertEquals(grunt.defense(), scaled.defense());
    }

    @Test
    void bonzoUsesWikiHealth() {
        assertEquals(250_000, DungeonMobRegistry.defaults().mob("BONZO").health());
        assertEquals(45_000, DungeonMobRegistry.defaults().mob("BONZO_UNDEAD").health());
    }

    @Test
    void estimatesOnlyForEntranceAndFloorOneNonBossMobs() {
        DungeonMobRegistry registry = DungeonMobRegistry.defaults();
        var estimate = registry.estimate("ZOMBIE_GRUNT", CatacombsFloor.FLOOR_ONE).orElseThrow();
        assertTrue(estimate.estimated());
        assertTrue(registry.estimate("ZOMBIE_GRUNT", CatacombsFloor.ENTRANCE).isEmpty());
        assertTrue(registry.estimate("BONZO", CatacombsFloor.FLOOR_ONE).isEmpty());
        assertTrue(registry.estimate("ZOMBIE_GRUNT", CatacombsFloor.FLOOR_TWO).isEmpty());
    }
}
