package dev.daweapon.type.skyblockgeneric.user.island.portal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.Map;

public final class PortalShape {
    public static final int CLEAR_HALF_WIDTH = 4;
    public static final int CLEAR_HALF_DEPTH = 2;
    public static final int CLEAR_HEIGHT = 15;
    private static final int[][] FRONT = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private PortalShape() {
    }

    public static int frontFromYaw(float playerYaw) {
        return Math.floorMod(Math.round(playerYaw / 90f), 4);
    }

    public static int[] world(PlacedPortal portal, int localX, int localZ) {
        int[] front = FRONT[portal.front()];
        int rightX = -front[1];
        int rightZ = front[0];
        return new int[]{
                portal.x() + localX * rightX + localZ * front[0],
                portal.z() + localX * rightZ + localZ * front[1]
        };
    }

    public static boolean hasClearance(Instance instance, PlacedPortal portal) {
        for (int lx = -CLEAR_HALF_WIDTH; lx <= CLEAR_HALF_WIDTH; lx++) {
            for (int lz = -CLEAR_HALF_DEPTH; lz <= CLEAR_HALF_DEPTH; lz++) {
                int[] column = world(portal, lx, lz);
                if (!instance.isChunkLoaded(column[0] >> 4, column[1] >> 4)) return false;
                for (int y = 0; y < CLEAR_HEIGHT; y++) {
                    Block block = instance.getBlock(column[0], portal.y() + y, column[1]);
                    if (block.isSolid() || block.isLiquid()) return false;
                }
            }
        }
        return true;
    }

    public static void build(Instance instance, PlacedPortal portal) {
        forEachBlock(portal.front(), (lx, ly, lz, block) -> {
            int[] column = world(portal, lx, lz);
            instance.setBlock(column[0], portal.y() + ly, column[1], block);
        });
    }

    public static void clear(Instance instance, PlacedPortal portal) {
        forEachBlock(portal.front(), (lx, ly, lz, block) -> {
            int[] column = world(portal, lx, lz);
            instance.setBlock(column[0], portal.y() + ly, column[1], Block.AIR);
        });
    }

    public static boolean isPartOf(PlacedPortal portal, int x, int y, int z) {
        boolean[] found = {false};
        forEachBlock(portal.front(), (lx, ly, lz, block) -> {
            int[] column = world(portal, lx, lz);
            if (column[0] == x && portal.y() + ly == y && column[1] == z) found[0] = true;
        });
        return found[0];
    }

    public static boolean isPortalPlane(PlacedPortal portal, int x, int y, int z) {
        for (int lx = -1; lx <= 1; lx++) {
            for (int ly = 0; ly <= 4; ly++) {
                int[] column = world(portal, lx, 0);
                if (column[0] == x && portal.y() + ly == y && column[1] == z) return true;
            }
        }
        return false;
    }

    public static Pos hologramPosition(PlacedPortal portal) {
        int[] column = world(portal, 0, 1);
        return new Pos(column[0] + 0.5, portal.y() + 1.5, column[1] + 0.5);
    }

    private static String cardinal(int dx, int dz) {
        if (dx > 0) return "east";
        if (dx < 0) return "west";
        return dz > 0 ? "south" : "north";
    }

    private static void forEachBlock(int frontIndex, BlockVisitor visitor) {
        int rightX = -FRONT[frontIndex][1];
        int rightZ = FRONT[frontIndex][0];
        fill(visitor, -1, 0, 0, 1, 4, 0, Block.NETHER_PORTAL);
        fill(visitor, -2, 0, 0, -2, 4, 0, Block.POLISHED_ANDESITE);
        fill(visitor, 2, 0, 0, 2, 4, 0, Block.POLISHED_ANDESITE);
        fill(visitor, -2, 5, 0, 2, 5, 0, Block.STONE);
        for (int lx : new int[]{-3, -1, 1, 3}) {
            visitor.accept(lx, 5, 1, Block.COBBLESTONE_WALL);
            visitor.accept(lx, 5, -1, Block.COBBLESTONE_WALL);
        }
        fill(visitor, -3, 6, -1, 3, 6, 1, Block.STONE_BRICK_SLAB);
        Block topSlab = Block.STONE_BRICK_SLAB.withProperties(Map.of("type", "top"));
        fill(visitor, -3, 4, -1, 3, 4, -1, topSlab);
        fill(visitor, -3, 4, 1, 3, 4, 1, topSlab);
        Block stairs = Block.STONE_BRICK_STAIRS.withProperties(Map.of("half", "top"));
        visitor.accept(-3, 4, 0, stairs.withProperty("facing", cardinal(rightX, rightZ)));
        visitor.accept(3, 4, 0, stairs.withProperty("facing", cardinal(-rightX, -rightZ)));
        visitor.accept(2, 0, 1, Block.COBBLESTONE_WALL);
        visitor.accept(-2, 0, 1, Block.COBBLESTONE_WALL);
        visitor.accept(2, 1, 1, Block.TORCH);
        visitor.accept(-2, 1, 1, Block.TORCH);
    }

    private static void fill(BlockVisitor visitor, int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        for (int x = x1; x <= x2; x++)
            for (int y = y1; y <= y2; y++)
                for (int z = z1; z <= z2; z++)
                    visitor.accept(x, y, z, block);
    }

    @FunctionalInterface
    private interface BlockVisitor {
        void accept(int localX, int localY, int localZ, Block block);
    }
}
