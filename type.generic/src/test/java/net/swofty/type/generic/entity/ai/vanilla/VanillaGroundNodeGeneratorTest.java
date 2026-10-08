package net.swofty.type.generic.entity.ai.vanilla;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VanillaGroundNodeGeneratorTest {
    private static final BoundingBox MOB = new BoundingBox(0.6, 1.95, 0.6);

    @Test
    void mobsWalkThroughShortGrassAndFlowers() {
        assertEquals(8, neighboursOn(Block.SHORT_GRASS, 1));
        assertEquals(8, neighboursOn(Block.POPPY, 1));
    }

    @Test
    void wallsStillBlockMovement() {
        assertEquals(0, neighboursOn(Block.STONE, 2));
    }

    private static int neighboursOn(Block cover, int height) {
        Block.Getter world = (x, y, z, condition) -> {
            if (y < 40) return Block.GRASS_BLOCK;
            if (y >= 40 && y < 40 + height && !(x == 0 && z == 0)) return cover;
            return Block.AIR;
        };
        PNode start = new PNode(new Pos(0.5, 40, 0.5), 0, 0, PNode.Type.WALK, null);
        return new VanillaGroundNodeGenerator()
                .getWalkable(world, new HashSet<>(), start, new Pos(10.5, 40, 0.5), MOB)
                .size();
    }
}
