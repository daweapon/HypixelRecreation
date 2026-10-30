package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.minestom.server.coordinate.Pos;

/**
 * Stand-in cavern used until a captured world exists at configuration/world/mineshaft/CODE.polar. It fills the
 * community-measured box every real shaft is pasted into (x -198..-106, y 0..33, z -198..-154) with a hollow
 * stone-brick cavern, an entrance platform, a ladder shaft down to the floor and a few test veins.
 */
public final class MineshaftPlaceholderLayout {
    public static final int MIN_X = -198;
    public static final int MAX_X = -106;
    public static final int MIN_Y = 0;
    public static final int MAX_Y = 33;
    public static final int MIN_Z = -198;
    public static final int MAX_Z = -154;
    public static final int FLOOR_Y = 4;
    public static final int PLATFORM_Y = 19;
    public static final int PLATFORM_MIN_X = -192;
    public static final int PLATFORM_MAX_X = -181;
    public static final int PLATFORM_MIN_Z = -178;
    public static final int PLATFORM_MAX_Z = -174;
    public static final int LADDER_X = -181;
    public static final int LADDER_Z = -176;
    public static final int LADDER_WALL_X = LADDER_X + 1;
    public static final int VEIN_Y = FLOOR_Y + 1;
    public static final int VEIN_Z = -170;
    public static final int VEIN_SIZE = 3;
    public static final int[] VEIN_ORIGINS_X = {-170, -164, -158, -152, -146};
    public static final Pos ENTRANCE = new Pos(-190.5, PLATFORM_Y + 1, -175.5, -90, 0);
    public static final Pos EXIT_MARKER = new Pos(-191.5, PLATFORM_Y + 1, -175.5);

    public enum Tile {AIR, WALL, FILL, PLATFORM, PILLAR, LADDER, VEIN_PRIMARY, VEIN_GLACITE, VEIN_UMBER, VEIN_TUNGSTEN, VEIN_TITANIUM}

    private static final Tile[] VEIN_TILES = {Tile.VEIN_PRIMARY, Tile.VEIN_GLACITE, Tile.VEIN_UMBER, Tile.VEIN_TUNGSTEN, Tile.VEIN_TITANIUM};

    private MineshaftPlaceholderLayout() {
    }

    public static boolean inBox(int x, int y, int z) {
        return x >= MIN_X && x <= MAX_X && y >= MIN_Y && y <= MAX_Y && z >= MIN_Z && z <= MAX_Z;
    }

    public static Tile tileAt(int x, int y, int z) {
        if (!inBox(x, y, z)) return Tile.AIR;
        if (x == MIN_X || x == MAX_X || y == MIN_Y || y == MAX_Y || z == MIN_Z || z == MAX_Z) return Tile.WALL;
        if (y <= FLOOR_Y) return Tile.FILL;
        if (z == LADDER_Z && y >= VEIN_Y && y <= PLATFORM_Y) {
            if (x == LADDER_X) return Tile.LADDER;
            if (x == LADDER_WALL_X) return Tile.PILLAR;
        }
        if (y == PLATFORM_Y && x >= PLATFORM_MIN_X && x <= PLATFORM_MAX_X && z >= PLATFORM_MIN_Z && z <= PLATFORM_MAX_Z) {
            return Tile.PLATFORM;
        }
        return veinAt(x, y, z);
    }

    private static Tile veinAt(int x, int y, int z) {
        if (y < VEIN_Y || y >= VEIN_Y + 2 || z < VEIN_Z || z >= VEIN_Z + VEIN_SIZE) return Tile.AIR;
        for (int index = 0; index < VEIN_ORIGINS_X.length; index++) {
            if (x >= VEIN_ORIGINS_X[index] && x < VEIN_ORIGINS_X[index] + VEIN_SIZE) return VEIN_TILES[index];
        }
        return Tile.AIR;
    }
}
