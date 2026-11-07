package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The Catacombs puzzles, named as the tab list shows them, with the first floor each can appear on (wiki puzzle table
 * and the 0.20.5 changelog: Higher Or Lower Floor III+, Ice Fill Floor VII only, Teleport Maze Floor I+) and the room
 * templates that hold them.
 */
public enum CatacombsPuzzle {
    CREEPER_BEAMS("Creeper Beams", CatacombsFloor.ENTRANCE, false, "creeper_beams"),
    THREE_WEIRDOS("Three Weirdos", CatacombsFloor.ENTRANCE, true, "three_weirdos"),
    TIC_TAC_TOE("Tic Tac Toe", CatacombsFloor.ENTRANCE, true, "tic_tac_toe"),
    WATER_BOARD("Water Board", CatacombsFloor.ENTRANCE, false, "water_board"),
    TELEPORT_MAZE("Teleport Maze", CatacombsFloor.FLOOR_ONE, false, "teleport_maze"),
    HIGHER_OR_LOWER("Higher Or Lower", CatacombsFloor.FLOOR_THREE, true, "blaze"),
    BOULDER("Boulder", CatacombsFloor.FLOOR_THREE, true, "boulder"),
    ICE_FILL("Ice Fill", CatacombsFloor.FLOOR_SEVEN, false, "ice_fill"),
    ICE_PATH("Ice Path", CatacombsFloor.FLOOR_THREE, true, "placeholder_puzzle_ice_path"),
    QUIZ("Quiz", CatacombsFloor.FLOOR_FOUR, true, "quiz");

    private final String displayName;
    private final CatacombsFloor minimumFloor;
    private final boolean failable;
    private final List<String> templateIds;

    CatacombsPuzzle(String displayName, CatacombsFloor minimumFloor, boolean failable, String... templateIds) {
        this.displayName = displayName;
        this.minimumFloor = minimumFloor;
        this.failable = failable;
        this.templateIds = List.of(templateIds);
    }

    public String displayName() {
        return displayName;
    }

    public CatacombsFloor minimumFloor() {
        return minimumFloor;
    }

    public boolean failable() {
        return failable;
    }

    public List<String> templateIds() {
        return templateIds;
    }

    public boolean canGenerate(CatacombsFloor floor) {
        return floor.ordinal() >= minimumFloor.ordinal();
    }

    public static Optional<CatacombsPuzzle> ofTemplate(String templateId) {
        return Arrays.stream(values()).filter(puzzle -> puzzle.templateIds.contains(templateId)).findFirst();
    }

    /** Whether a puzzle room template may be placed on the floor; rooms that hold no known puzzle are allowed anywhere. */
    public static boolean templateAllowedOn(String templateId, CatacombsFloor floor) {
        return ofTemplate(templateId).map(puzzle -> puzzle.canGenerate(floor)).orElse(true);
    }
}
