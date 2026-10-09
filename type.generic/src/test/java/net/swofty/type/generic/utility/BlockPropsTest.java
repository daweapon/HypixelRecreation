package net.swofty.type.generic.utility;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockTags;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockPropsTest {
    @Test
    void classifiesTheSameBlocksAsTheVanillaTags() {
        Registry<Block> blocks = Block.staticRegistry();
        RegistryTag<Block> fences = blocks.getTag(BlockTags.FENCES);
        RegistryTag<Block> walls = blocks.getTag(BlockTags.WALLS);
        RegistryTag<Block> doors = blocks.getTag(BlockTags.DOORS);
        RegistryTag<Block> trapdoors = blocks.getTag(BlockTags.TRAPDOORS);
        for (Block block : Block.values()) {
            assertEquals(fences.contains(blocks.getKey(block)) || walls.contains(blocks.getKey(block)),
                    BlockProps.isFence(block), block.name());
            assertEquals(doors.contains(blocks.getKey(block)), BlockProps.isDoor(block), block.name());
            assertEquals(trapdoors.contains(blocks.getKey(block)) || block == Block.LILY_PAD || block == Block.BIG_DRIPLEAF,
                    BlockProps.isTrapdoor(block), block.name());
        }
        assertTrue(BlockProps.isFence(Block.OAK_FENCE.withProperty("north", "true")));
        assertTrue(BlockProps.isFence(Block.COBBLESTONE_WALL));
        assertTrue(BlockProps.isFenceGate(Block.OAK_FENCE_GATE));
        assertTrue(BlockProps.isWoodenDoor(Block.OAK_DOOR));
        assertTrue(BlockProps.isIronDoor(Block.IRON_DOOR));
        assertTrue(BlockProps.isClimbable(Block.LADDER));
        assertTrue(BlockProps.isLeaves(Block.OAK_LEAVES));
        assertTrue(BlockProps.isBurningBlock(Block.FIRE));
        assertTrue(BlockProps.isLitCampfire(Block.CAMPFIRE.withProperty("lit", "true")));
        assertFalse(BlockProps.isFence(Block.STONE));
        assertFalse(BlockProps.isDoor(Block.GRASS_BLOCK));
    }

    @Test
    void reportsTagLookupCost() {
        List<Block> sample = List.of(Block.STONE, Block.GRASS_BLOCK, Block.NETHERRACK, Block.OAK_FENCE, Block.AIR,
                Block.BLACKSTONE, Block.CRIMSON_NYLIUM, Block.MAGMA_BLOCK);
        Registry<Block> blocks = Block.staticRegistry();
        RegistryTag<Block> fences = blocks.getTag(BlockTags.FENCES);
        int rounds = 200_000;
        int hits = 0;
        for (int warm = 0; warm < 2; warm++) {
            for (int i = 0; i < 20_000; i++) for (Block block : sample) if (BlockProps.isFence(block)) hits++;
            for (int i = 0; i < 20_000; i++) for (Block block : sample) if (fences.contains(blocks.getKey(block))) hits++;
        }
        long start = System.nanoTime();
        for (int i = 0; i < rounds; i++) for (Block block : sample) if (fences.contains(blocks.getKey(block))) hits++;
        long tagNanos = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 0; i < rounds; i++) for (Block block : sample) if (BlockProps.isFence(block)) hits++;
        long fastNanos = System.nanoTime() - start;
        System.out.printf("fence check x%d: RegistryTag %d ms, BlockProps %d ms (%d hits)%n",
                rounds * sample.size(), tagNanos / 1_000_000, fastNanos / 1_000_000, hits);
    }
}
