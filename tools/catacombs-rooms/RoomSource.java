import java.util.List;
import java.util.Map;

/**
 * Where a room is cut from: the cell grid of its world, the doors between cells, and per room the measured data that
 * would otherwise be estimated.
 */
final class RoomSource {
    static final int NO_ANNEX = Integer.MIN_VALUE;

    private RoomSource() {
    }

    /** {@code label} is the catalogue's own name for the secret ("3 - Chest"), whose number pairs it with its lever or wall. */
    record Secret(String type, String variant, double x, double y, double z, boolean counted, boolean estimated, String label) {
    }

    /** {@code seenAs} is the higher-floor mob that stood here when it differs from the Floor 1 {@code mobId}. */
    record Spawn(String mobId, double x, double y, double z, boolean starred, boolean starGuessed, String seenAs) {
    }

    record Crypt(int x, int y, int z) {
    }

    /** A point of the room's secret route that is not a scored secret: lever, superboom wall, stonk block, entrance. */
    record Extra(String kind, String label, int x, int y, int z) {
    }

    /**
     * One room to cut. {@code cryptTarget} is how many crypt spots the room should offer (measured ones first, the rest
     * estimated); {@code placed} holds block states, by "x,y,z", that the source world only received after it was saved.
     * {@code extraTurns} turns the room further by quarter turns that keep its shape, so a replacement keeps the frame of
     * the template it replaces.
     */
    record Room(String id, String name, String kind, int[][] cells, int minY, int maxY, List<Secret> secrets,
                List<Spawn> spawns, List<Crypt> crypts, int cryptTarget, List<Extra> extras, Map<String, String> placed,
                double[] spawnPoint, int annexMinZ, String folder, String tag, String catalogueName, int extraTurns) {
        Room withTurns(int turns) {
            return new Room(id, name, kind, cells, minY, maxY, secrets, spawns, crypts, cryptTarget, extras, placed, spawnPoint,
                    annexMinZ, folder, tag, catalogueName, turns);
        }

        Room withId(String newId) {
            return new Room(newId, name, kind, cells, minY, maxY, secrets, spawns, crypts, cryptTarget, extras, placed, spawnPoint,
                    annexMinZ, folder, tag, catalogueName, extraTurns);
        }
    }

    /**
     * A 32-block cell grid. Cell (0,0) touches the origin corner and further cells grow along {@code sign}: the M7
     * map grows toward negative X and Z, Hypixel's dungeon toward positive.
     */
    record Grid(int originX, int originZ, int sign, int[][][] doors) {
        static final int PITCH = 32;
        static final int ROOM = 31;

        int cellMinX(int gx) {
            return sign > 0 ? originX + PITCH * gx : originX - PITCH * gx - (ROOM - 1);
        }

        int cellMaxX(int gx) {
            return cellMinX(gx) + ROOM - 1;
        }

        int cellMinZ(int gz) {
            return sign > 0 ? originZ + PITCH * gz : originZ - PITCH * gz - (ROOM - 1);
        }

        int cellMaxZ(int gz) {
            return cellMinZ(gz) + ROOM - 1;
        }

        int along(int worldX) {
            return sign * (worldX - originX);
        }

        int alongZ(int worldZ) {
            return sign * (worldZ - originZ);
        }

        int localIndex(int g, int gMin, int gMax) {
            return sign > 0 ? g - gMin : gMax - g;
        }

        int seamX(int low) {
            return sign > 0 ? cellMaxX(low) + 1 : cellMinX(low) - 1;
        }

        int seamZ(int low) {
            return sign > 0 ? cellMaxZ(low) + 1 : cellMinZ(low) - 1;
        }

        int sideTowards(int[] own, int[] other) {
            if (other[0] != own[0]) return (other[0] - own[0]) * sign > 0 ? 1 : 3;
            return (other[1] - own[1]) * sign > 0 ? 2 : 0;
        }
    }
}
