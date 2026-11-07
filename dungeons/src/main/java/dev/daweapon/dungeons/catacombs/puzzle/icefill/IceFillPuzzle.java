package dev.daweapon.dungeons.catacombs.puzzle.icefill;

import java.util.List;

/** The boards of the Ice Fill room, walked in any order; the puzzle is solved once every board is done. */
public final class IceFillPuzzle {
    public record Outcome(int board, IceFillStep step) {
        public static final Outcome NONE = new Outcome(-1, IceFillStep.IGNORED);
    }

    private final List<IceFillProgress> boards;

    public IceFillPuzzle(List<IceFillBoard> boards) {
        this.boards = boards.stream().map(IceFillProgress::new).toList();
    }

    public Outcome step(int y, IceTile tile) {
        for (int index = 0; index < boards.size(); index++) {
            IceFillProgress progress = boards.get(index);
            if (progress.board().y() == y && progress.board().contains(tile)) return new Outcome(index, progress.step(tile));
        }
        return Outcome.NONE;
    }

    /** The board whose tiles lie under a column, by height, or -1 when the column is off every board. */
    public int boardAt(int y, IceTile tile) {
        for (int index = 0; index < boards.size(); index++) {
            IceFillBoard board = boards.get(index).board();
            if (board.y() == y && board.contains(tile)) return index;
        }
        return -1;
    }

    public IceFillProgress board(int index) {
        return boards.get(index);
    }

    public int size() {
        return boards.size();
    }

    public boolean solved() {
        return boards.stream().allMatch(IceFillProgress::done);
    }
}
