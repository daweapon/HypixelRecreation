package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillBoard;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillProgress;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillStep;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceTile;
import dev.daweapon.type.catacombs.engine.GateLift;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ice Fill (M7 run 3 capture): three layers of ice; every tile walked on turns to packed ice, each finished layer
 * plays three rising harp notes, and once all three are done the iron-bar gate sinks away to two blessing chests.
 * Stepping back onto packed ice, a diagonal step or a jump shatters the layer (wiki): it turns back to ice and the player
 * is put back on its first tile.
 */
public final class IceFillRoom extends PuzzleRoom {
    private static final float[][] BOARD_NOTES = {{1.19f, 1.286f, 1.397f}, {1.492f, 1.587f, 1.698f}, {1.794f, 1.889f, 2.0f}};
    private static final int NOTE_TICKS = 5;
    private static final int GATE_CLICKS = 8;
    private static final double JUMP_HEIGHT = 0.75;

    private record Tile(int board, IceTile tile) {
    }

    private final PuzzleCatalogue.IceFillRoom data;
    private final IceFillPuzzle puzzle;
    private final Map<Long, Tile> tiles = new HashMap<>();
    private final Map<Integer, Integer> surfaceByBoard = new HashMap<>();
    private final List<PuzzleChest> chests = new ArrayList<>();

    public IceFillRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.IceFillRoom data) {
        super(context, room, CatacombsPuzzle.ICE_FILL);
        this.data = data;
        this.puzzle = new IceFillPuzzle(data.boards());
    }

    @Override
    public void start() {
        for (int index = 0; index < data.boards().size(); index++) {
            IceFillBoard board = data.boards().get(index);
            for (IceTile tile : board.tiles()) {
                int[] at = frame.block(new RoomBlock(tile.x(), board.y(), tile.z()));
                tiles.put(RoomFrame.key(at[0], 0, at[2]), new Tile(index, tile));
                surfaceByBoard.put(index, at[1] + 1);
            }
        }
        data.chests().forEach(local -> chests.add(chest(frame.block(local))));
        resetAll();
    }

    private void resetAll() {
        for (int index = 0; index < puzzle.size(); index++) paint(index, Block.ICE);
    }

    private void paint(int board, Block block) {
        IceFillBoard layout = puzzle.board(board).board();
        for (IceTile tile : layout.tiles()) setTile(layout, tile, block);
    }

    private void setTile(IceFillBoard board, IceTile tile, Block block) {
        int[] at = frame.block(new RoomBlock(tile.x(), board.y(), tile.z()));
        instance().setBlock(at[0], at[1], at[2], block);
    }

    @Override
    public synchronized void moved(SkyBlockPlayer player, Pos to, boolean onGround) {
        if (puzzle.solved()) return;
        Tile under = tiles.get(RoomFrame.key(to.blockX(), 0, to.blockZ()));
        if (under == null) return;
        IceFillProgress progress = puzzle.board(under.board());
        if (progress.done()) return;
        double surface = surfaceByBoard.get(under.board());
        if (progress.started() && to.y() > surface + JUMP_HEIGHT) {
            shatter(player, under.board());
            return;
        }
        if (Math.abs(to.y() - surface) > 0.1 || !onGround) return;
        IceFillStep step = progress.step(under.tile());
        switch (step) {
            case PACKED -> setTile(progress.board(), under.tile(), Block.PACKED_ICE);
            case BOARD_DONE -> boardDone(under.board(), under.tile());
            case SHATTERED -> shatter(player, under.board());
            case IGNORED -> {
            }
        }
    }

    private void boardDone(int board, IceTile last) {
        IceFillProgress progress = puzzle.board(board);
        setTile(progress.board(), last, Block.PACKED_ICE);
        Pos at = frame.centre(new RoomBlock(last.x(), progress.board().y() + 1, last.z()));
        float[] notes = BOARD_NOTES[Math.min(board, BOARD_NOTES.length - 1)];
        for (int note = 0; note < notes.length; note++) {
            float pitch = notes[note];
            later(NOTE_TICKS * note + 1, () -> sound("block.note_block.harp", 1f, pitch, at.x(), at.y(), at.z()));
        }
        advance(CatacombsPuzzleFactory.BOARD);
        if (puzzle.solved()) openGate();
    }

    private void shatter(SkyBlockPlayer player, int board) {
        IceFillProgress progress = puzzle.board(board);
        progress.reset();
        paint(board, Block.ICE);
        Pos at = player.getPosition();
        sound("block.glass.break", 1f, 1f, at.x(), at.y(), at.z());
        IceTile start = progress.board().start();
        Pos entry = frame.centre(new RoomBlock(start.x(), progress.board().y() + 1, start.z()));
        player.teleport(entry.withView(at.yaw(), at.pitch()));
    }

    private void openGate() {
        List<int[]> gate = data.gate().stream().map(frame::block).toList();
        GateLift.run(instance(), gate, _ -> Block.IRON_BARS, GateLift.Motion.LEVER);
        Pos centre = frame.centre(data.gate().get(data.gate().size() / 2));
        for (int click = 0; click < GATE_CLICKS; click++) {
            later(NOTE_TICKS * click + 1, () -> sound("block.wooden_pressure_plate.click_on", 2f, 0.492f, centre.x(), centre.y(), centre.z()));
        }
        chests.forEach(PuzzleChest::unlock);
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        for (PuzzleChest chest : chests) {
            if (!chest.at(x, y, z)) continue;
            chest.click(player);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        for (int index = 0; index < puzzle.size(); index++) {
            IceFillProgress progress = puzzle.board(index);
            if (progress.done()) continue;
            progress.complete();
            paint(index, Block.PACKED_ICE);
            advance(CatacombsPuzzleFactory.BOARD);
        }
        if (chests.stream().noneMatch(PuzzleChest::unlocked)) openGate();
    }

    @Override
    public List<String> info() {
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < puzzle.size(); index++) {
            IceFillProgress progress = puzzle.board(index);
            lines.add("<7>Layer " + (index + 1) + ": <e>" + progress.walked().size() + "<7>/<e>" + progress.board().tiles().size()
                    + (progress.done() ? " <a>done" : ""));
        }
        return lines;
    }
}
