package dev.daweapon.type.catacombs.engine;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

/** The placeholder boss arena: a walled stone-brick box beside the dungeon grid. */
final class BossArenaBox {
    static final int SIZE = 41;
    private static final int WALL_HEIGHT = 12;

    private final int minX;
    private final int minZ;
    private final int floorY;

    BossArenaBox(int minX, int minZ, int floorY) {
        this.minX = minX;
        this.minZ = minZ;
        this.floorY = floorY;
    }

    Pos spawn() {
        return new Pos(minX + SIZE / 2.0, floorY + 1, minZ + 5.5);
    }

    Pos bossSpot() {
        return new Pos(minX + SIZE / 2.0, floorY + 1, minZ + SIZE - 8);
    }

    void build(Instance instance) {
        for (int chunkX = minX >> 4; chunkX <= (minX + SIZE) >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (minZ + SIZE) >> 4; chunkZ++) {
                instance.loadChunk(chunkX, chunkZ).join();
            }
        }
        for (int x = minX - 1; x <= minX + SIZE; x++) {
            for (int z = minZ - 1; z <= minZ + SIZE; z++) {
                boolean edge = x < minX || z < minZ || x >= minX + SIZE || z >= minZ + SIZE;
                instance.setBlock(x, floorY, z, edge ? Block.DEEPSLATE_BRICKS : Block.STONE_BRICKS);
                for (int y = floorY + 1; y <= floorY + WALL_HEIGHT; y++) {
                    instance.setBlock(x, y, z, edge ? Block.DEEPSLATE_BRICKS : Block.AIR);
                }
                instance.setBlock(x, floorY + WALL_HEIGHT + 1, z, Block.BARRIER);
            }
        }
    }
}
