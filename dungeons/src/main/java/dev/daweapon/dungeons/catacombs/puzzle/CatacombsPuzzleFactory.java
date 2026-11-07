package dev.daweapon.dungeons.catacombs.puzzle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CatacombsPuzzleFactory {
    public static final String BEAMS_ALIGNED = "beams_aligned";
    public static final String DOORS_OPEN = "doors_open";
    public static final String CENTRE_REACHED = "centre_reached";
    public static final String BOARD = "board";
    public static final String CHEST_REACHED = "chest_reached";
    public static final String RIGHT_CHEST = "right_chest";
    public static final String TIE = "tie";
    public static final String PATH_OPEN = "path_open";
    public static final int ICE_FILL_BOARDS = 3;

    public static PuzzleController create(CatacombsPuzzle puzzle) {
        return switch (puzzle) {
            case CREEPER_BEAMS -> new ObjectivePuzzleController(puzzle, BEAMS_ALIGNED, 1);
            case WATER_BOARD -> new ObjectivePuzzleController(puzzle, DOORS_OPEN, 1);
            case TELEPORT_MAZE -> new ObjectivePuzzleController(puzzle, CENTRE_REACHED, 1);
            case ICE_FILL -> new ObjectivePuzzleController(puzzle, BOARD, ICE_FILL_BOARDS);
            case ICE_PATH -> new ObjectivePuzzleController(puzzle, CHEST_REACHED, 1);
            case THREE_WEIRDOS -> new OrderedPuzzleController(puzzle, List.of(RIGHT_CHEST));
            case TIC_TAC_TOE -> new OrderedPuzzleController(puzzle, List.of(TIE));
            case HIGHER_OR_LOWER -> new OrderedPuzzleController(puzzle,
                    List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
            case BOULDER -> new ObjectivePuzzleController(puzzle, PATH_OPEN, 1);
            case QUIZ -> new OrderedPuzzleController(puzzle, List.of("question_1", "question_2", "question_3"));
        };
    }
}
