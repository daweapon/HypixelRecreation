package dev.daweapon.type.skyblockgeneric.user.island;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandBiomesTest {
    @Test
    void offersPlainsPlusEveryBiomeStick() {
        assertEquals(13, IslandBiomes.choices().size());
        assertEquals("Plains", IslandBiomes.choices().getFirst().name());
    }

    @Test
    void chunkRadiusCoversTheBorderAndIsCapped() {
        assertEquals(7, IslandBiomes.chunkRadius(161));
        assertEquals(9, IslandBiomes.chunkRadius(241));
        assertEquals(IslandBiomes.MAX_CHUNK_RADIUS, IslandBiomes.chunkRadius(59_999_968));
        assertTrue(IslandBiomes.chunkRadius(241) <= IslandBiomes.MAX_CHUNK_RADIUS);
    }
}
