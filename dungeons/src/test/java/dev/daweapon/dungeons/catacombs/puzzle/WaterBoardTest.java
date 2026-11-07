package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.waterboard.WaterBoard;
import dev.daweapon.dungeons.catacombs.puzzle.waterboard.WaterBoardSolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterBoardTest {
    private static final List<String> DOORS = List.of("red", "lime", "blue", "orange", "purple");
    private static final String CAPTURED_AFTER_33_UPDATES = String.join("\n",
            "##########8##########",
            "##########|##########",
            "##########|##########",
            "##########|##########",
            "#####....Q|7654321..#",
            "#####.#########C#|#.#",
            "#####E..........#|#.#",
            "#####.######.####|#.#",
            "#.... . ..#E.... |E.#",
            "#.#### ##.#.#####|###",
            "#.####.##.#.#####|###",
            "#.###..#E. . .E67|76#",
            "#.###.##.######|###|#",
            "#... .Q..######|#67|#",
            "# #.###########|#|###",
            "#.#.###########|#|#.#",
            "#.#...... 34567|#|#.#",
            "#.########|######|###",
            "#.###....7|D.... | .#",
            "#...Q.####T##.###|#.#",
            "#E### ####.E..#67|#.#",
            "#.###.####.####.###.#",
            "#.###.####.####.###.#",
            "#...................#",
            "#.###.####.####.###.#");

    private static WaterBoard.Layout layout() {
        return PuzzleCatalogueFixture.load().waterBoard("water_board").orElseThrow().layout();
    }

    private static String draw(WaterBoard board) {
        List<String> lines = new ArrayList<>();
        WaterBoard.Layout layout = board.layout();
        for (int row = 0; row < layout.height(); row++) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < layout.width(); column++) {
                int z = board.z(column), y = board.y(row);
                WaterBoard.Gate gate = board.gates().stream().filter(each -> each.z() == z && each.y() == y).findFirst().orElse(null);
                if (board.flow().wet(column, row)) {
                    line.append(board.flow().falling(column, row) ? '|' : (char) ('0' + board.flow().amount(column, row)));
                } else if (gate != null) {
                    line.append(gate.extended() ? Character.toUpperCase(gate.type().charAt(0)) : ' ');
                } else {
                    line.append(layout.rows().get(row).charAt(column) == '#' ? '#' : '.');
                }
            }
            lines.add(line.toString());
        }
        return String.join("\n", lines);
    }

    @Test
    void theBoardHasSixKindsOfGateFiveHolesAndAWaterLever() {
        PuzzleCatalogue.WaterBoardRoom room = PuzzleCatalogueFixture.load().waterBoard("water_board").orElseThrow();
        assertEquals(6, WaterBoardSolver.gateTypes(room.layout()).size());
        assertEquals(DOORS, room.layout().holes().stream().map(WaterBoard.Hole::door).toList());
        assertEquals(7, room.levers().size());
        assertTrue(room.levers().stream().anyMatch(lever -> lever.type().equals(WaterBoard.WATER_LEVER)));
        assertEquals(5, room.doors().size());
        assertEquals(3, room.closedDoors());
    }

    @Test
    void theWaterRunsExactlyAsCapturedOnHypixel() {
        WaterBoard board = new WaterBoard(layout(), Set.of("orange", "blue", "purple"));
        board.pull("gold_block");
        board.pull("quartz_block");
        board.pull(WaterBoard.WATER_LEVER);
        List<String> reached = new ArrayList<>();
        for (int update = 1; update <= 80; update++) {
            if (update == 34) board.pull("emerald_block");
            if (update == 43) board.pull("coal_block");
            int at = update;
            board.step().forEach(door -> reached.add(at + " " + door));
            if (update == 33) assertEquals(CAPTURED_AFTER_33_UPDATES, draw(board));
        }
        assertEquals(List.of("37 orange", "41 purple", "43 lime", "55 blue"), reached);
        assertEquals(Map.of("red", true, "lime", false, "blue", true, "orange", true, "purple", true), board.doors());
        assertFalse(board.allDoorsOpen());
    }

    @Test
    void theWaterDriesUpOnceTurnedOff() {
        WaterBoard board = new WaterBoard(layout(), Set.of());
        board.setWater(true);
        for (int update = 0; update < 60; update++) board.step();
        board.setWater(false);
        for (int update = 0; update < 120 && !board.settled(); update++) board.step();
        WaterBoard.Layout layout = board.layout();
        for (int row = 0; row < layout.height(); row++) {
            for (int column = 0; column < layout.width(); column++) assertFalse(board.flow().wet(column, row), row + "," + column);
        }
    }

    @Test
    void aGatePushedIntoWaterRemovesIt() {
        WaterBoard board = new WaterBoard(layout(), Set.of());
        board.pull("gold_block");
        board.setWater(true);
        for (int update = 0; update < 80; update++) board.step();
        WaterBoard.Gate wetGate = board.gates().stream()
                .filter(gate -> !gate.extended() && board.flow().wet(board.column(gate.z()), board.row(gate.y())))
                .findFirst().orElseThrow();
        board.pull(wetGate.type());
        assertFalse(board.flow().wet(board.column(wetGate.z()), board.row(wetGate.y())));
    }

    @Test
    void everyChoiceOfThreeClosedDoorsTheRoomCanDealHasAWorkingPlan() {
        WaterBoard.Layout layout = layout();
        List<WaterBoardSolver.Run> runs = WaterBoardSolver.runs(layout);
        assertEquals(64, runs.size());
        int solvable = 0;
        for (int first = 0; first < DOORS.size(); first++) {
            for (int second = first + 1; second < DOORS.size(); second++) {
                for (int third = second + 1; third < DOORS.size(); third++) {
                    Set<String> closed = Set.of(DOORS.get(first), DOORS.get(second), DOORS.get(third));
                    if (!WaterBoardSolver.canToggle(layout, runs, closed)) continue;
                    solvable++;
                    assertTrue(followPlan(layout, closed, WaterBoardSolver.plan(layout, runs, closed)), closed.toString());
                }
            }
        }
        assertTrue(solvable > 0, "no set of three closed doors can be opened");
        assertTrue(WaterBoardSolver.canToggle(layout, runs, Set.of("orange", "blue", "purple")), "the captured doors must be solvable");
    }

    private static boolean followPlan(WaterBoard.Layout layout, Set<String> closed, List<WaterBoardSolver.Run> plan) {
        WaterBoard board = new WaterBoard(layout, closed);
        Set<String> pulled = new HashSet<>();
        for (WaterBoardSolver.Run run : plan) {
            for (String type : WaterBoardSolver.gateTypes(layout)) {
                if (pulled.contains(type) != run.levers().contains(type)) {
                    board.pull(type);
                    if (!pulled.remove(type)) pulled.add(type);
                }
            }
            board.setWater(true);
            for (int update = 0; update < WaterBoardSolver.MAX_STEPS && !board.settled(); update++) board.step();
            board.setWater(false);
            for (int update = 0; update < WaterBoardSolver.MAX_STEPS && !board.settled(); update++) board.step();
        }
        return board.allDoorsOpen();
    }
}
