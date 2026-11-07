package dev.daweapon.dungeons.catacombs.puzzle.icefill;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

/**
 * The walk across one board. Every tile must be stepped on once, starting from the board's entry tile and only moving
 * to a side neighbour; stepping back onto packed ice or landing anywhere else (a diagonal move or a jump) shatters it.
 */
public final class IceFillProgress {
    private final IceFillBoard board;
    private final SequencedSet<IceTile> walked = new LinkedHashSet<>();

    public IceFillProgress(IceFillBoard board) {
        this.board = board;
    }

    public IceFillStep step(IceTile tile) {
        if (done() || !board.contains(tile)) return IceFillStep.IGNORED;
        if (walked.isEmpty()) {
            if (!tile.equals(board.start())) return IceFillStep.SHATTERED;
            return mark(tile);
        }
        IceTile last = walked.getLast();
        if (tile.equals(last)) return IceFillStep.IGNORED;
        if (walked.contains(tile) || !last.touches(tile)) return IceFillStep.SHATTERED;
        return mark(tile);
    }

    private IceFillStep mark(IceTile tile) {
        walked.add(tile);
        return done() ? IceFillStep.BOARD_DONE : IceFillStep.PACKED;
    }

    public boolean done() {
        return walked.size() == board.tiles().size();
    }

    public boolean started() {
        return !walked.isEmpty();
    }

    public List<IceTile> walked() {
        return new ArrayList<>(walked);
    }

    public void reset() {
        walked.clear();
    }

    public void complete() {
        walked.addAll(board.tiles());
    }

    public IceFillBoard board() {
        return board;
    }
}
