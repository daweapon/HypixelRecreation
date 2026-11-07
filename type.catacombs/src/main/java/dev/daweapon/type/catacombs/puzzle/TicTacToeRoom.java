package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeAi;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Mark;
import dev.daweapon.dungeons.catacombs.puzzle.tictactoe.TicTacToeBoard.Result;
import dev.daweapon.type.catacombs.engine.GateLift;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tic Tac Toe (M7 run 2 capture): nine stone buttons on the wall. Hypixel has already played when the party arrives;
 * pressing a button swaps it for the player's O on a map and Hypixel answers with an X three seconds later. A tie (or,
 * against a perfect opponent, never a win) solves the puzzle: the reward chest appears and the wall behind the board
 * sinks away. Losing fails the puzzle.
 */
public final class TicTacToeRoom extends PuzzleRoom {
    private static final int ANSWER_TICKS = 60;
    private static final int WALL_DELAY_TICKS = 30;
    private static final int CLICK_TICKS = 5;
    private static final int WALL_CLICKS = 7;
    private static final String LOST = "lost";

    private final PuzzleCatalogue.TicTacToeRoom data;
    private final TicTacToeBoard board = new TicTacToeBoard();
    private final List<int[]> cells = new ArrayList<>();
    private final List<Entity> frames = new ArrayList<>();
    private PuzzleChest chest;
    private Block chestBlock;
    private Direction facing = Direction.SOUTH;
    private boolean thinking;

    public TicTacToeRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.TicTacToeRoom data) {
        super(context, room, CatacombsPuzzle.TIC_TAC_TOE);
        this.data = data;
    }

    @Override
    public void start() {
        data.cells().forEach(local -> cells.add(frame.block(local)));
        Block button = cells.stream().map(at -> context.host().arena().blocks().blockAt(at[0], at[1], at[2]))
                .filter(block -> block != null && block.compare(Block.STONE_BUTTON)).findFirst().orElse(null);
        if (button != null && button.getProperty("facing") != null) facing = Direction.valueOf(button.getProperty("facing").toUpperCase(Locale.ROOT));
        Block fresh = Block.STONE_BUTTON.withProperty("face", "wall").withProperty("facing", facing.name().toLowerCase(Locale.ROOT));
        cells.forEach(at -> instance().setBlock(at[0], at[1], at[2], fresh));
        int[] chestAt = frame.block(data.chest());
        Block original = context.host().arena().blocks().blockAt(chestAt[0], chestAt[1], chestAt[2]);
        chestBlock = original != null && original.compare(Block.CHEST) ? original : Block.CHEST;
        instance().setBlock(chestAt[0], chestAt[1], chestAt[2], Block.AIR);
        chest = chest(chestAt);
        mark(TicTacToeAi.opening(context.floor(), ThreadLocalRandom.current()), Mark.HYPIXEL);
    }

    private void mark(int cell, Mark mark) {
        board.place(cell, mark);
        int[] at = cells.get(cell);
        instance().setBlock(at[0], at[1], at[2], Block.AIR);
        Entity frameEntity = TicTacToeMarks.frame(TicTacToeMarks.packet(mark, mark == Mark.HYPIXEL ? data.hypixelMark() : data.playerMark()), facing);
        frameEntity.setInstance(instance(), TicTacToeMarks.at(at));
        frames.add(frameEntity);
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        if (chest.at(x, y, z)) {
            chest.click(player);
            return true;
        }
        int cell = cellAt(x, y, z);
        if (cell < 0) return false;
        if (over() || thinking || !board.free(cell)) return true;
        mark(cell, Mark.PLAYER);
        Pos at = player.getPosition();
        sound("block.note_block.pling", 1f, 2f, at.x(), at.y(), at.z());
        if (settle(player)) return true;
        thinking = true;
        later(ANSWER_TICKS, () -> answer(player));
        return true;
    }

    private int cellAt(int x, int y, int z) {
        for (int cell = 0; cell < cells.size(); cell++) {
            int[] at = cells.get(cell);
            if (at[0] == x && at[1] == y && at[2] == z) return cell;
        }
        return -1;
    }

    private synchronized void answer(SkyBlockPlayer player) {
        thinking = false;
        if (over() || board.result() != Result.PLAYING) return;
        mark(TicTacToeAi.move(board, ThreadLocalRandom.current()), Mark.HYPIXEL);
        settle(player);
    }

    /** Ends the game when the last move finished it; true when it did. */
    private boolean settle(@Nullable SkyBlockPlayer player) {
        Result result = board.result();
        if (result == Result.PLAYING) return false;
        if (result == Result.HYPIXEL_WON) {
            select(LOST);
            fail(player, player == null ? null : PuzzleMessages.failed(player, "lost Tic Tac Toe!"));
            return true;
        }
        tied(player);
        return true;
    }

    private void tied(@Nullable SkyBlockPlayer player) {
        select(CatacombsPuzzleFactory.TIE);
        if (player != null) context.host().broadcast(PuzzleMessages.solved(player, "tied Tic Tac Toe!"));
        int[] at = chest.position();
        instance().setBlock(at[0], at[1], at[2], chestBlock);
        chest.unlock();
        later(WALL_DELAY_TICKS, this::openWall);
    }

    private void openWall() {
        List<int[]> wall = data.wall().stream().map(frame::block).toList();
        List<Block> looks = wall.stream().map(at -> instance().getBlock(at[0], at[1], at[2])).toList();
        GateLift.run(instance(), wall, at -> looks.get(wall.indexOf(at)), GateLift.Motion.LEVER);
        Pos centre = frame.centre(data.wall().get(data.wall().size() / 2));
        for (int click = 0; click < WALL_CLICKS; click++) {
            later(CLICK_TICKS * click + 1, () -> sound("block.wooden_pressure_plate.click_on", 2f, 0.492f, centre.x(), centre.y(), centre.z()));
        }
        later(GateLift.Motion.LEVER.openTick(), this::clearBoard);
    }

    private synchronized void clearBoard() {
        frames.forEach(Entity::remove);
        frames.clear();
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        if (over()) return;
        thinking = false;
        tied(staff);
    }

    @Override
    public List<String> info() {
        StringBuilder rows = new StringBuilder();
        List<String> lines = new ArrayList<>();
        for (int cell = 0; cell < TicTacToeBoard.CELLS; cell++) {
            rows.append(board.at(cell).map(mark -> mark == Mark.HYPIXEL ? "<c>X" : "<b>O").orElse("<8>-"));
            if (cell % 3 == 2) {
                lines.add(rows.toString());
                rows.setLength(0);
            }
        }
        lines.add("<7>Result: <e>" + board.result());
        return lines;
    }

    @Override
    public void dispose() {
        clearBoard();
    }
}
