package dev.daweapon.type.crystalhollows.quest;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KingsScentWaterTest {
    @Test
    void waterAtTheFeetOrTheEyesWashesTheScentOff() {
        assertTrue(KingsScentWater.touchesWater(_ -> Block.WATER, new Pos(0, 64, 0)));
        assertTrue(KingsScentWater.touchesWater(point -> point.y() >= 65.5 ? Block.WATER : Block.AIR, new Pos(0, 64, 0)));
    }

    @Test
    void dryAirAndWaterloggedBlocksAreToldApart() {
        assertFalse(KingsScentWater.touchesWater(_ -> Block.AIR, new Pos(0, 64, 0)));
        assertTrue(KingsScentWater.isWet(Block.OAK_SLAB.withProperty("waterlogged", "true")));
        assertFalse(KingsScentWater.isWet(Block.STONE));
    }
}
