import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The sixteen rooms of the MIT-licensed M7 practice map (github.com/strad-dev/m7, instructions/clear/Rooms.java),
 * copied as data: cells on its 32-block grid where world (-10,-10) is cell (0,0) and the grid grows toward negative
 * X/Z, the measured Y range, and every secret at its world position.
 */
final class M7Rooms {
    static final int ORIGIN = -10;
    static final int PITCH = 32;
    static final int DOOR_FLOOR_Y = 68;
    static final int START_ANNEX_MIN_Z = -213;
    static final double[] START_SPAWN = {-120.5, 71, -183.5};

    static final int[][][] DOORS = {
            {{0, 0}, {0, 1}}, {{4, 0}, {5, 0}}, {{5, 1}, {5, 2}}, {{3, 2}, {4, 2}}, {{3, 2}, {3, 1}},
            {{3, 2}, {3, 3}}, {{2, 2}, {2, 1}}, {{0, 1}, {0, 2}}, {{3, 3}, {3, 4}}, {{3, 4}, {3, 5}},
            {{4, 4}, {4, 5}}, {{5, 4}, {5, 5}}, {{3, 3}, {2, 3}}, {{2, 5}, {1, 5}}, {{0, 4}, {1, 4}}
    };

    static final RoomSource.Grid GRID = new RoomSource.Grid(ORIGIN, ORIGIN, -1, DOORS);

    private M7Rooms() {
    }

    private static final class Builder {
        private final String id;
        private final String name;
        private final String kind;
        private final int[][] cells;
        private final int minY;
        private final int maxY;
        private final List<RoomSource.Secret> secrets = new ArrayList<>();

        Builder(String id, String name, String kind, int[][] cells, int minY, int maxY) {
            this.id = id;
            this.name = name;
            this.kind = kind;
            this.cells = cells;
            this.minY = minY;
            this.maxY = maxY;
        }

        Builder chest(double x, double y, double z) {
            return add("CHEST", "CHEST", x, y, z, true);
        }

        Builder blessingChest(double x, double y, double z, String blessing) {
            return add("CHEST", "BLESSING_" + blessing, x, y, z, true);
        }

        Builder mimicChest(double x, double y, double z) {
            return add("CHEST", "MIMIC", x, y, z, true);
        }

        Builder rewardChest(double x, double y, double z, String blessing) {
            return add("CHEST", "PUZZLE_REWARD_" + blessing, x, y, z, false);
        }

        Builder item(double x, double y, double z) {
            return add("ITEM", "ITEM", x, y, z, true);
        }

        Builder bat(double x, double y, double z) {
            return add("BAT", "BAT", x, y, z, true);
        }

        Builder essence(double x, double y, double z) {
            return add("ESSENCE", "ESSENCE", x, y, z, true);
        }

        private Builder add(String type, String variant, double x, double y, double z, boolean counted) {
            secrets.add(new RoomSource.Secret(type, variant, x, y, z, counted, false, null));
            return this;
        }

        RoomSource.Room build() {
            boolean start = id.equals("start");
            return new RoomSource.Room(id, name, kind, cells, minY, maxY, List.copyOf(secrets), List.of(), List.of(), 3,
                    List.of(), Map.of(), start ? START_SPAWN : null, start ? START_ANNEX_MIN_Z : RoomSource.NO_ANNEX, "m7", "m7", null, 0);
        }
    }

    static List<RoomSource.Room> all() {
        List<RoomSource.Room> rooms = new ArrayList<>();
        rooms.add(new Builder("quiz", "Quiz", "PUZZLE", new int[][]{{0, 0}}, 46, 105).build());
        rooms.add(new Builder("hallway", "Hallway", "NORMAL", new int[][]{{1, 0}, {2, 0}, {3, 0}, {4, 0}}, 66, 99)
                .chest(-140, 69, -37).chest(-114, 69, -35).chest(-83, 85, -34).build());
        rooms.add(new Builder("market", "Market", "NORMAL", new int[][]{{5, 0}, {4, 1}, {5, 1}}, 56, 99)
                .chest(-186, 79, -26).chest(-186, 61, -40).chest(-194, 86, -56).essence(-182, 59, -65)
                .bat(-181.5, 63, -61.5).build());
        rooms.add(new Builder("gravel", "Gravel", "NORMAL", new int[][]{{0, 1}, {1, 1}, {2, 1}}, 56, 99)
                .chest(-69, 69, -61).bat(-68.5, 91, -57.5).essence(-12, 69, -69).item(-16.5, 69, -63.5)
                .essence(-38, 87, -58).item(-36.5, 87, -59.5).build());
        rooms.add(new Builder("blood", "Blood", "BLOOD", new int[][]{{3, 1}}, 66, 99).build());
        rooms.add(new Builder("museum", "Museum", "NORMAL", new int[][]{{4, 2}, {5, 2}, {4, 3}, {5, 3}}, 58, 119)
                .chest(-169, 70, -134).essence(-180, 93, -127).blessingChest(-172, 83, -85, "STONE_2")
                .mimicChest(-169, 70, -83).blessingChest(-186, 62, -80, "STONE_2").build());
        rooms.add(new Builder("deathmite", "Deathmite", "NORMAL", new int[][]{{1, 2}, {2, 2}, {3, 2}}, 56, 99)
                .item(-130.5, 69, -79.5).blessingChest(-111, 60, -84, "LIFE_2").blessingChest(-109, 82, -89, "LIFE_2")
                .blessingChest(-125, 92, -101, "LIFE_2").bat(-123.5, 95, -98.5).chest(-54, 69, -89).build());
        rooms.add(new Builder("dino_dig_site", "Dino Site", "NORMAL", new int[][]{{0, 2}, {0, 3}, {1, 3}}, 36, 101)
                .chest(-34, 92, -103).bat(-17.5, 47, -106.5).item(-55.5, 57, -110.5).chest(-64, 52, -125).build());
        rooms.add(new Builder("wizard", "Wizard", "NORMAL", new int[][]{{2, 3}, {2, 4}, {2, 5}}, 46, 99)
                .item(-94.5, 76, -196.5).chest(-100, 92, -183).bat(-83.5, 53, -179.5).chest(-98, 89, -110).build());
        rooms.add(new Builder("fairy", "Fairy", "FAIRY", new int[][]{{3, 3}}, 62, 99).build());
        rooms.add(new Builder("well", "Well", "NORMAL", new int[][]{{0, 4}, {0, 5}, {1, 5}}, 53, 119)
                .blessingChest(-70, 89, -185, "WISDOM_2").item(-68.5, 91, -173.5).blessingChest(-22, 88, -188, "WISDOM_1")
                .essence(-12, 95, -197).essence(-17, 95, -194).chest(-29, 91, -163).item(-22.5, 57, -154.5).build());
        rooms.add(new Builder("ice_fill", "Ice Fill", "PUZZLE", new int[][]{{1, 4}}, 55, 90)
                .rewardChest(-71, 75, -152, "POWER_5").rewardChest(-71, 75, -154, "POWER_5").build());
        rooms.add(new Builder("red_blue", "Red Blue", "NORMAL", new int[][]{{3, 4}, {4, 4}, {5, 4}}, 66, 99)
                .essence(-185, 83, -153).item(-165.5, 86, -159.5).blessingChest(-145, 90, -164, "POWER_2")
                .chest(-163, 70, -143).build());
        rooms.add(new Builder("start", "Start", "ENTRANCE", new int[][]{{3, 5}}, 66, 98).build());
        rooms.add(new Builder("trap", "Trap", "TRAP", new int[][]{{4, 5}}, 60, 100)
                .chest(-143, 67, -182).bat(-158.5, 92, -190.5).blessingChest(-164, 90, -184, "POWER_2").build());
        rooms.add(new Builder("yellow", "Yellow", "MINIBOSS", new int[][]{{5, 5}}, 64, 99).build());
        return rooms;
    }
}
