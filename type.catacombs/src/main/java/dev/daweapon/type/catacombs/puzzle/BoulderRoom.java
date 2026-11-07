package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard.Cell;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard.Face;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicRotation;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Boulder (M7 runs 1, 2 and 4, one captured layout each): wooden boxes faced with signs fill a 7 x 6 grid between the
 * stairs and the chest. Pressing a box's button pushes it one box away from the button, into an empty space only, and a
 * box pushed off the edge vanishes. The room is solved when the chest at the back is opened.
 */
public final class BoulderRoom extends PuzzleRoom {
    private record Button(Cell cell, Face face) {
    }

    private final PuzzleCatalogue.BoulderRoom data;
    private final Map<Long, Block> shown = new HashMap<>();
    private final Map<Long, Button> buttons = new HashMap<>();
    private final int turns;
    private BoulderBoard board;
    private PuzzleChest chest;

    public BoulderRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.BoulderRoom data) {
        super(context, room, CatacombsPuzzle.BOULDER);
        this.data = data;
        this.turns = room.rotation().quarterTurns();
    }

    @Override
    public void start() {
        board = new BoulderBoard(data.columns(), data.rows(),
                data.variants().get(ThreadLocalRandom.current().nextInt(data.variants().size())));
        chest = chest(frame.block(data.chest()));
        chest.unlock();
        render();
    }

    private void render() {
        Map<RoomBlock, Block> wanted = new HashMap<>();
        int size = data.cellSize();
        for (int x = data.gridX(); x < data.gridX() + data.columns() * size; x++) {
            for (int z = data.gridZ() - 1; z <= data.gridZ() + data.rows() * size; z++) {
                for (int y = data.gridY(); y < data.gridY() + size; y++) wanted.put(new RoomBlock(x, y, z), Block.AIR);
            }
        }
        buttons.clear();
        for (Map.Entry<Cell, BoulderBoard.Box> entry : board.boxes().entrySet()) {
            Cell cell = entry.getKey();
            Block wood = Objects.requireNonNull(Block.fromKey(entry.getValue().wood()));
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    for (int y = 0; y < size; y++) wanted.put(local(cell, dx, y, dz), wood);
                }
            }
        }
        for (Map.Entry<Cell, BoulderBoard.Box> entry : board.boxes().entrySet()) {
            for (Face face : Face.values()) faceBlocks(entry.getKey(), face, entry.getValue().buttons().contains(face), wanted);
        }
        wanted.forEach(this::set);
    }

    private void faceBlocks(Cell cell, Face face, boolean hasButton, Map<RoomBlock, Block> wanted) {
        int size = data.cellSize();
        int middle = size / 2;
        for (int along = 0; along < size; along++) {
            for (int y = 0; y < size; y++) {
                int dx = face.dx() == 0 ? along : face.dx() < 0 ? -1 : size;
                int dz = face.dz() == 0 ? along : face.dz() < 0 ? -1 : size;
                RoomBlock at = local(cell, dx, y, dz);
                Block current = wanted.get(at);
                if (current == null || !current.air()) continue;
                String facing = face.name().toLowerCase(Locale.ROOT);
                boolean button = hasButton && along == middle && y == middle;
                wanted.put(at, button
                        ? Block.STONE_BUTTON.withProperty("face", "wall").withProperty("facing", facing)
                        : Block.OAK_WALL_SIGN.withProperty("facing", facing));
                if (button) buttons.put(RoomFrame.key(frame.block(at)), new Button(cell, face));
            }
        }
    }

    private RoomBlock local(Cell cell, int dx, int y, int dz) {
        int size = data.cellSize();
        return new RoomBlock(data.gridX() + cell.column() * size + dx, data.gridY() + y, data.gridZ() + cell.row() * size + dz);
    }

    private void set(RoomBlock local, Block block) {
        int[] at = frame.block(local);
        long key = RoomFrame.key(at);
        Block turned = block.air() ? block : SchematicRotation.rotate(block, turns);
        if (turned.equals(shown.get(key))) return;
        shown.put(key, turned);
        instance().setBlock(at[0], at[1], at[2], turned);
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        if (chest != null && chest.at(x, y, z)) {
            if (!over()) solved();
            chest.click(player);
            return true;
        }
        Button button = buttons.get(RoomFrame.key(x, y, z));
        if (button == null) return false;
        BoulderBoard.Push push = board.press(button.cell(), button.face());
        if (push == BoulderBoard.Push.MOVED || push == BoulderBoard.Push.REMOVED) render();
        return true;
    }

    private void solved() {
        advance(CatacombsPuzzleFactory.PATH_OPEN);
        completeRoom();
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        List<BoulderBoard.Press> presses = board.shortestSolution(200_000).orElse(List.of());
        for (BoulderBoard.Press press : presses) board.press(press.cell(), press.face());
        render();
    }

    @Override
    public List<String> info() {
        List<String> lines = new ArrayList<>();
        lines.add("<7>Way to the chest: " + (board.pathOpen() ? "<a>open" : "<c>shut") + "<7>, boxes left <e>" + board.boxes().size());
        board.shortestSolution(200_000).ifPresent(presses -> presses.forEach(press -> lines.add("<7>Press the <e>"
                + press.face().name().toLowerCase(Locale.ROOT) + " <7>button of the box in column <e>" + (press.cell().column() + 1)
                + "<7>, row <e>" + (press.cell().row() + 1))));
        return lines;
    }
}
