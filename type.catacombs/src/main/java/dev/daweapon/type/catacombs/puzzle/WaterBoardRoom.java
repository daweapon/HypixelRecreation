package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzleFactory;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.dungeons.catacombs.puzzle.RoomBlock;
import dev.daweapon.dungeons.catacombs.puzzle.waterboard.WaterBoard;
import dev.daweapon.dungeons.catacombs.puzzle.waterboard.WaterBoardSolver;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicRotation;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Water Board (F1 run 1 capture): six levers push their kind of gate block in and out of the maze, a seventh at the
 * back lets water in at the top. The water runs through the maze one block per water update, and every time it reaches
 * one of the five holes at the bottom that hole's coloured door toggles. Three doors start closed; the chest behind them
 * opens once all five are open together.
 */
public final class WaterBoardRoom extends PuzzleRoom {
    private static final int GATE_BEHIND = 1;
    private static final int PISTON_BEHIND = 2;
    private static final Map<String, List<WaterBoardSolver.Run>> RUNS = new ConcurrentHashMap<>();

    private final PuzzleCatalogue.WaterBoardRoom data;
    private final Map<Long, String> levers = new HashMap<>();
    private final Map<Long, Block> shown = new HashMap<>();
    private final int turns;
    private WaterBoard board;
    private PuzzleChest chest;
    private List<WaterBoardSolver.Run> runs = List.of();

    public WaterBoardRoom(PuzzleContext context, PlacedRoom room, PuzzleCatalogue.WaterBoardRoom data) {
        super(context, room, CatacombsPuzzle.WATER_BOARD);
        this.data = data;
        this.turns = room.rotation().quarterTurns();
    }

    @Override
    public void start() {
        runs = RUNS.computeIfAbsent(room.template().id(), _ -> WaterBoardSolver.runs(data.layout()));
        board = new WaterBoard(data.layout(), closedDoors());
        data.levers().forEach(lever -> levers.put(RoomFrame.key(frame.block(lever.at())), lever.type()));
        chest = chest(frame.block(data.chest()));
        for (WaterBoard.Gate gate : board.gates()) showGate(gate);
        setPlane(data.layout().sourceZ(), data.layout().sourceY(), Block.fromKey(data.sourcePlug()));
        for (PuzzleCatalogue.WaterDoor door : data.doors()) showDoor(door, board.doorOpen(door.colour()), false);
        clearPlaneWater();
    }

    private Set<String> closedDoors() {
        List<String> doors = new ArrayList<>(data.doors().stream().map(PuzzleCatalogue.WaterDoor::colour).toList());
        List<Set<String>> choices = new ArrayList<>();
        collect(doors, 0, new ArrayList<>(), choices);
        Collections.shuffle(choices, ThreadLocalRandom.current());
        return choices.stream().filter(choice -> WaterBoardSolver.canToggle(data.layout(), runs, choice)).findFirst()
                .orElse(Set.of());
    }

    private void collect(List<String> doors, int from, List<String> picked, List<Set<String>> choices) {
        if (picked.size() == data.closedDoors()) {
            choices.add(Set.copyOf(picked));
            return;
        }
        for (int index = from; index < doors.size(); index++) {
            picked.add(doors.get(index));
            collect(doors, index + 1, picked, choices);
            picked.removeLast();
        }
    }

    private void clearPlaneWater() {
        WaterBoard.Layout layout = data.layout();
        for (int row = 0; row < layout.height(); row++) {
            for (int column = 0; column < layout.width(); column++) {
                if (layout.rows().get(row).charAt(column) != '.') continue;
                setPlane(board.z(column), board.y(row), Block.AIR);
            }
        }
    }

    @Override
    public synchronized boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        if (chest != null && chest.at(x, y, z)) {
            chest.click(player);
            return true;
        }
        String type = levers.get(RoomFrame.key(x, y, z));
        if (type == null) return false;
        Block lever = instance().getBlock(x, y, z);
        boolean powered = !"true".equals(lever.getProperty("powered"));
        if (lever.compare(Block.LEVER)) instance().setBlock(x, y, z, lever.withProperty("powered", String.valueOf(powered)));
        sound("block.lever.click", 0.3f, powered ? 0.6f : 0.5f, x + 0.5, y + 0.5, z + 0.5);
        List<WaterBoard.Gate> moved = board.pull(type);
        moved.forEach(this::showGate);
        if (WaterBoard.WATER_LEVER.equals(type)) {
            setPlane(data.layout().sourceZ(), data.layout().sourceY(), board.waterOn() ? Block.WATER : Block.fromKey(data.sourcePlug()));
        } else if (!moved.isEmpty()) {
            Pos at = frame.centre(new RoomBlock(data.planeX(), moved.getFirst().y(), moved.getFirst().z()));
            sound(moved.getFirst().extended() ? "block.piston.extend" : "block.piston.contract", 0.5f, 1f, at.x(), at.y(), at.z());
        }
        return true;
    }

    @Override
    public synchronized void poll() {
        if (board == null) return;
        if (board.settled() && !board.waterOn()) return;
        for (String door : board.step()) {
            data.doors().stream().filter(each -> each.colour().equals(door)).findFirst()
                    .ifPresent(each -> showDoor(each, board.doorOpen(door), true));
        }
        showWater();
        if (board.allDoorsOpen() && !chest.unlocked()) solved();
    }

    private void showWater() {
        WaterBoard.Layout layout = data.layout();
        for (int row = 0; row < layout.height(); row++) {
            for (int column = 0; column < layout.width(); column++) {
                if (layout.rows().get(row).charAt(column) == '#') continue;
                int z = board.z(column), y = board.y(row);
                if (z == layout.sourceZ() && y == layout.sourceY()) continue;
                if (gateExtendedAt(z, y)) continue;
                setPlane(z, y, water(column, row));
            }
        }
    }

    private Block water(int column, int row) {
        if (!board.flow().wet(column, row)) return Block.AIR;
        if (board.flow().falling(column, row)) return Block.WATER.withProperty("level", "8");
        return Block.WATER.withProperty("level", String.valueOf(8 - board.flow().amount(column, row)));
    }

    private boolean gateExtendedAt(int z, int y) {
        return board.gates().stream().anyMatch(gate -> gate.extended() && gate.z() == z && gate.y() == y);
    }

    private void showGate(WaterBoard.Gate gate) {
        Block block = Block.fromKey(gate.type());
        Block piston = turned(Block.STICKY_PISTON.withProperty("facing", "east").withProperty("extended", String.valueOf(gate.extended())));
        set(new RoomBlock(data.planeX() - PISTON_BEHIND, gate.y(), gate.z()), piston);
        if (gate.extended()) {
            set(new RoomBlock(data.planeX() - GATE_BEHIND, gate.y(), gate.z()),
                    turned(Block.PISTON_HEAD.withProperty("facing", "east").withProperty("type", "sticky")));
            setPlane(gate.z(), gate.y(), Objects.requireNonNull(block));
        } else {
            set(new RoomBlock(data.planeX() - GATE_BEHIND, gate.y(), gate.z()), Objects.requireNonNull(block));
            setPlane(gate.z(), gate.y(), Block.AIR);
        }
    }

    private void showDoor(PuzzleCatalogue.WaterDoor door, boolean open, boolean audible) {
        Block wool = Objects.requireNonNull(Block.fromKey("minecraft:" + door.colour() + "_wool"));
        for (PuzzleCatalogue.DoorPart part : data.doorFill()) set(new RoomBlock(door.x(), part.y(), part.z()), open ? Block.AIR : wool);
        for (PuzzleCatalogue.DoorPart part : data.doorHeads()) {
            RoomBlock at = new RoomBlock(door.x(), part.y(), part.z());
            set(at, open ? original(at) : turned(Block.PISTON_HEAD.withProperty("facing", part.facing()).withProperty("type", "sticky")));
        }
        for (PuzzleCatalogue.DoorPart part : data.doorPistons()) {
            RoomBlock at = new RoomBlock(door.x(), part.y(), part.z());
            set(at, open ? original(at) : turned(Block.STICKY_PISTON.withProperty("facing", part.facing()).withProperty("extended", "true")));
        }
        if (!audible) return;
        Pos at = frame.centre(new RoomBlock(door.x(), data.doorFill().getFirst().y(), data.doorFill().getFirst().z()));
        sound(open ? "block.piston.contract" : "block.piston.extend", 0.5f, 1f, at.x(), at.y(), at.z());
    }

    private Block original(RoomBlock local) {
        int[] at = frame.block(local);
        Block block = context.host().arena().blocks().blockAt(at[0], at[1], at[2]);
        return block == null ? Block.AIR : block;
    }

    private Block turned(Block block) {
        return SchematicRotation.rotate(block, turns);
    }

    private void setPlane(int z, int y, Block block) {
        set(new RoomBlock(data.planeX(), y, z), block);
    }

    private void set(RoomBlock local, Block block) {
        int[] at = frame.block(local);
        long key = RoomFrame.key(at);
        if (block.equals(shown.get(key))) return;
        shown.put(key, block);
        instance().setBlock(at[0], at[1], at[2], block);
    }

    @Override
    public synchronized void solve(SkyBlockPlayer staff) {
        board.openAllDoors();
        data.doors().forEach(door -> showDoor(door, true, true));
        if (!chest.unlocked()) solved();
    }

    private void solved() {
        advance(CatacombsPuzzleFactory.DOORS_OPEN);
        chest.unlock();
        completeRoom();
    }

    @Override
    public List<String> info() {
        List<String> lines = new ArrayList<>();
        Set<String> closed = new HashSet<>();
        board.doors().forEach((door, open) -> {
            if (!open) closed.add(door);
        });
        lines.add("<7>Closed doors: <e>" + (closed.isEmpty() ? "none" : String.join(", ", closed)) + "<7>, water "
                + (board.waterOn() ? "<a>on" : "<c>off"));
        for (WaterBoardSolver.Run run : WaterBoardSolver.plan(data.layout(), runs, closed)) {
            lines.add("<7>Water with levers <e>" + (run.levers().isEmpty() ? "none" : String.join(", ", run.levers()))
                    + " <7>toggles <e>" + String.join(", ", run.toggled()));
        }
        return lines;
    }
}
