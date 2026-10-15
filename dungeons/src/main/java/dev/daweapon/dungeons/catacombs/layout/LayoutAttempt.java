package dev.daweapon.dungeons.catacombs.layout;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

final class LayoutAttempt {
    private static final double EXTRA_DOOR_CHANCE = 0.2;
    private static final double RARE_CHANCE = 0.1;
    private static final int MAX_PATH_NUMERATOR = 3;
    private static final int MAX_PATH_DENOMINATOR = 5;

    private record Draft(RoomKind kind, RoomShape shape, Rotation rotation, List<Cell> cells) {
        Cell firstCell() {
            return cells.getFirst();
        }
    }

    private record Edge(int low, int high) {
        static Edge of(int first, int second) {
            return new Edge(Math.min(first, second), Math.max(first, second));
        }
    }

    private final CatacombsFloor floor;
    private final CatacombsMode mode;
    private final long seed;
    private final RoomTemplateLibrary library;
    private final RoomBudget fixedBudget;
    private final Random random;
    private final int width;
    private final int height;
    private final Map<String, ArrayDeque<RoomTemplate>> pools = new HashMap<>();

    LayoutAttempt(CatacombsFloor floor, CatacombsMode mode, long seed, RoomTemplateLibrary library,
                  RoomBudget fixedBudget, Random random) {
        this.floor = floor;
        this.mode = mode;
        this.seed = seed;
        this.library = library;
        this.fixedBudget = fixedBudget;
        this.random = random;
        this.width = floor.dungeonSize().width();
        this.height = floor.dungeonSize().height();
    }

    FloorLayout build() {
        Cell start = new Cell(random.nextInt(width), height - 1);
        Cell blood = new Cell(random.nextInt(width), 0);
        List<Cell> path = CriticalPathFinder
                .find(width, height, start, blood, height + 1, width * height * MAX_PATH_NUMERATOR / MAX_PATH_DENOMINATOR, random)
                .orElse(null);
        if (path == null) {
            return null;
        }
        Map<Cell, Integer> pathIndex = new HashMap<>();
        for (int index = 0; index < path.size(); index++) {
            pathIndex.put(path.get(index), index);
        }
        Map<Cell, RoomKind> specialCells = placeSpecialCells(path);
        if (specialCells == null) {
            return null;
        }
        List<Draft> drafts = draftRooms(specialCells, pathIndex);
        List<PlacedRoom> rooms = placeRooms(drafts, new HashSet<>(path));
        Map<Cell, Integer> roomOfCell = new HashMap<>();
        rooms.forEach(room -> room.cells().forEach(cell -> roomOfCell.put(cell, room.index())));

        List<Integer> roomPath = roomPath(path, roomOfCell);
        if (roomPath == null) {
            return null;
        }
        List<LayoutDoor> doors = buildDoors(path, roomPath, rooms, roomOfCell);
        if (doors == null) {
            return null;
        }
        return new FloorLayout(floor, mode, seed, width, height, rooms, doors, roomPath);
    }

    private Map<Cell, RoomKind> placeSpecialCells(List<Cell> path) {
        Map<Cell, RoomKind> special = new LinkedHashMap<>();
        special.put(path.getFirst(), RoomKind.ENTRANCE);
        special.put(path.getLast(), RoomKind.BLOOD);
        special.put(path.get(pickFairyIndex(path.size())), RoomKind.FAIRY);

        Cell beforeBlood = path.get(path.size() - 2);
        List<Cell> candidates = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = new Cell(col, row);
                if (!special.containsKey(cell) && !cell.equals(beforeBlood)) {
                    candidates.add(cell);
                }
            }
        }
        Collections.shuffle(candidates, random);

        RoomBudget budget = fixedBudget != null ? fixedBudget : RoomBudget.forFloor(floor, random);
        int puzzles = Math.min(budget.puzzles(), library.templates(RoomKind.PUZZLE).size());
        int minibosses = Math.min(budget.minibosses(), library.templates(RoomKind.MINIBOSS).size());
        int traps = Math.min(budget.traps(), library.templates(RoomKind.TRAP).size());
        if (candidates.size() < puzzles + minibosses + traps) {
            return null;
        }
        int next = 0;
        for (int count = 0; count < puzzles; count++) {
            special.put(candidates.get(next++), RoomKind.PUZZLE);
        }
        for (int count = 0; count < minibosses; count++) {
            special.put(candidates.get(next++), RoomKind.MINIBOSS);
        }
        for (int count = 0; count < traps; count++) {
            special.put(candidates.get(next++), RoomKind.TRAP);
        }
        return special;
    }

    private int pickFairyIndex(int pathLength) {
        int low = Math.max(1, (pathLength + 2) / 3);
        int high = Math.max(low, Math.min(pathLength - 3, pathLength * 2 / 3));
        return low + random.nextInt(high - low + 1);
    }

    private List<Draft> draftRooms(Map<Cell, RoomKind> specialCells, Map<Cell, Integer> pathIndex) {
        boolean[][] reserved = new boolean[width][height];
        List<Draft> drafts = new ArrayList<>();
        specialCells.forEach((cell, kind) -> {
            reserved[cell.col()][cell.row()] = true;
            drafts.add(new Draft(kind, RoomShape.ONE_BY_ONE, randomRotation(), List.of(cell)));
        });

        Set<RoomShape> allowedShapes = EnumSet.noneOf(RoomShape.class);
        for (RoomShape shape : RoomShape.values()) {
            if (library.has(RoomKind.NORMAL, shape)) {
                allowedShapes.add(shape);
            }
        }
        int rareRemaining = library.templates(RoomKind.RARE).size();
        for (RoomTiler.Tile tile : RoomTiler.tile(width, height, reserved, pathIndex, allowedShapes, random)) {
            RoomKind kind = RoomKind.NORMAL;
            if (tile.shape() == RoomShape.ONE_BY_ONE && rareRemaining > 0 && random.nextDouble() < RARE_CHANCE) {
                kind = RoomKind.RARE;
                rareRemaining--;
            }
            drafts.add(new Draft(kind, tile.shape(), tile.rotation(), tile.cells()));
        }
        drafts.sort(Comparator.comparingInt((Draft draft) -> draft.firstCell().row())
                .thenComparingInt(draft -> draft.firstCell().col()));
        return drafts;
    }

    private Rotation randomRotation() {
        return Rotation.values()[random.nextInt(Rotation.values().length)];
    }

    private List<PlacedRoom> placeRooms(List<Draft> drafts, Set<Cell> pathCells) {
        List<PlacedRoom> rooms = new ArrayList<>();
        for (int index = 0; index < drafts.size(); index++) {
            Draft draft = drafts.get(index);
            int minCol = draft.cells().stream().mapToInt(Cell::col).min().orElseThrow();
            int minRow = draft.cells().stream().mapToInt(Cell::row).min().orElseThrow();
            boolean onPath = draft.cells().stream().anyMatch(pathCells::contains);
            rooms.add(new PlacedRoom(index, nextTemplate(draft.kind(), draft.shape()), draft.rotation(),
                    new Cell(minCol, minRow), draft.cells(), onPath));
        }
        return rooms;
    }

    private RoomTemplate nextTemplate(RoomKind kind, RoomShape shape) {
        ArrayDeque<RoomTemplate> pool = pools.computeIfAbsent(kind + "/" + shape, key -> new ArrayDeque<>());
        if (pool.isEmpty()) {
            List<RoomTemplate> refill = new ArrayList<>(library.templates(kind, shape));
            Collections.shuffle(refill, random);
            pool.addAll(refill);
        }
        return pool.removeFirst();
    }

    private List<Integer> roomPath(List<Cell> path, Map<Cell, Integer> roomOfCell) {
        List<Integer> roomPath = new ArrayList<>();
        for (Cell cell : path) {
            int room = roomOfCell.get(cell);
            if (!roomPath.isEmpty() && roomPath.getLast() == room) {
                continue;
            }
            if (roomPath.contains(room)) {
                return null;
            }
            roomPath.add(room);
        }
        return roomPath;
    }

    private List<LayoutDoor> buildDoors(List<Cell> path, List<Integer> roomPath, List<PlacedRoom> rooms,
                                        Map<Cell, Integer> roomOfCell) {
        int bloodRoom = roomPath.getLast();
        List<LayoutDoor> doors = new ArrayList<>();
        Set<Edge> doored = new HashSet<>();
        for (int step = 0; step < path.size() - 1; step++) {
            Cell from = path.get(step);
            Cell to = path.get(step + 1);
            int fromRoom = roomOfCell.get(from);
            int toRoom = roomOfCell.get(to);
            if (fromRoom == toRoom) {
                continue;
            }
            DoorType type = toRoom == bloodRoom ? DoorType.BLOOD : doors.isEmpty() ? DoorType.PLAIN : DoorType.WITHER;
            doors.add(new LayoutDoor(from, to, fromRoom, toRoom, type));
            doored.add(Edge.of(fromRoom, toRoom));
        }

        Map<Edge, List<Cell[]>> candidates = adjacentRoomEdges(roomOfCell);
        int[] anchor = new int[rooms.size()];
        Arrays.fill(anchor, -1);
        roomPath.forEach(room -> anchor[room] = room);

        while (true) {
            List<Edge> frontier = candidates.keySet().stream()
                    .filter(edge -> edge.low() != bloodRoom && edge.high() != bloodRoom)
                    .filter(edge -> (anchor[edge.low()] >= 0) != (anchor[edge.high()] >= 0))
                    .toList();
            if (frontier.isEmpty()) {
                break;
            }
            Edge edge = frontier.get(random.nextInt(frontier.size()));
            int connected = anchor[edge.low()] >= 0 ? edge.low() : edge.high();
            int joining = connected == edge.low() ? edge.high() : edge.low();
            anchor[joining] = anchor[connected];
            doors.add(plainDoor(edge, candidates.get(edge), roomOfCell));
            doored.add(edge);
        }
        for (int room : anchor) {
            if (room < 0) {
                return null;
            }
        }

        for (Map.Entry<Edge, List<Cell[]>> entry : candidates.entrySet()) {
            Edge edge = entry.getKey();
            boolean sameBranch = anchor[edge.low()] == anchor[edge.high()];
            if (sameBranch && edge.low() != bloodRoom && edge.high() != bloodRoom && !doored.contains(edge)
                    && random.nextDouble() < EXTRA_DOOR_CHANCE) {
                doors.add(plainDoor(edge, entry.getValue(), roomOfCell));
                doored.add(edge);
            }
        }
        return doors;
    }

    private LayoutDoor plainDoor(Edge edge, List<Cell[]> sides, Map<Cell, Integer> roomOfCell) {
        Cell[] pair = sides.get(random.nextInt(sides.size()));
        return new LayoutDoor(pair[0], pair[1], roomOfCell.get(pair[0]), roomOfCell.get(pair[1]), DoorType.PLAIN);
    }

    private Map<Edge, List<Cell[]>> adjacentRoomEdges(Map<Cell, Integer> roomOfCell) {
        Map<Edge, List<Cell[]>> edges = new LinkedHashMap<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Cell cell = new Cell(col, row);
                for (Cell neighbour : List.of(cell.translate(1, 0), cell.translate(0, 1))) {
                    Integer other = roomOfCell.get(neighbour);
                    if (other != null && !other.equals(roomOfCell.get(cell))) {
                        edges.computeIfAbsent(Edge.of(roomOfCell.get(cell), other), key -> new ArrayList<>())
                                .add(new Cell[]{cell, neighbour});
                    }
                }
            }
        }
        return edges;
    }
}
