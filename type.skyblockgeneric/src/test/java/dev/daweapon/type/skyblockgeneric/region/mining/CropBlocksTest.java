package dev.daweapon.type.skyblockgeneric.region.mining;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropBlocksTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void cropBlocksMapToTheirItems() {
        assertEquals(Material.POTATO, CropBlocks.materialOf(Block.POTATOES));
        assertEquals(Material.CARROT, CropBlocks.materialOf(Block.CARROTS));
        assertEquals(Material.WHEAT, CropBlocks.materialOf(Block.WHEAT));
        assertEquals(Material.MELON, CropBlocks.materialOf(Block.MELON));
    }

    @Test
    void cropsGrowBackFullyGrown() {
        assertEquals("7", CropBlocks.fullyGrown(Block.POTATOES).getProperty("age"));
        assertEquals("7", CropBlocks.fullyGrown(Block.CARROTS.withProperty("age", "2")).getProperty("age"));
        assertEquals("7", CropBlocks.fullyGrown(Block.WHEAT).getProperty("age"));
        assertEquals(Block.MELON, CropBlocks.fullyGrown(Block.MELON));
    }
}
