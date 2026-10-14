package net.swofty.type.skywarsgame.luckyblock;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LuckyBlockTypeTest {
    @Test
    void fromBlockMatchesEachGlassColour() {
        for (LuckyBlockType type : LuckyBlockType.values()) {
            assertSame(type, LuckyBlockType.fromBlock(type.getGlassBlock()));
            assertTrue(type.matches(type.getGlassBlock()));
        }
    }

    @Test
    void ordinaryBlocksAreNotLuckyBlocks() {
        assertNull(LuckyBlockType.fromBlock(Block.STONE));
        assertFalse(LuckyBlockType.isLuckyBlock(Block.GLASS));
        assertTrue(LuckyBlockType.isLuckyBlock(Block.CYAN_STAINED_GLASS));
    }

    @Test
    void fromNameIsExactAndNullWhenUnknown() {
        assertEquals(LuckyBlockType.OP_RULE, LuckyBlockType.fromName("OP_RULE"));
        assertNull(LuckyBlockType.fromName("op_rule"));
        assertNull(LuckyBlockType.fromName("missing"));
    }

    @Test
    void glassBlocksAndTexturesAreUnique() {
        Set<Block> blocks = new HashSet<>();
        Set<String> textures = new HashSet<>();
        for (LuckyBlockType type : LuckyBlockType.values()) {
            assertTrue(blocks.add(type.getGlassBlock()), type.name());
            assertTrue(textures.add(type.getSkullTexture()), type.name());
        }
    }
}
