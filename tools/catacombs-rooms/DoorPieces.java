import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hypixel's doorways. A door between two rooms is a piece Hypixel pastes over both rooms' outer walls and the seam
 * between them: 5 wide, from the floor to 5 blocks up, two layers deep into each room (the inner layer is the arch you
 * see from inside the room). The piece's look differs per door, not per room. Where a room has no door its wall is the
 * room's own closed wall, so a room cut from a sighting where a socket was used gets that wall from another sighting of
 * the room where the socket was not used, or an estimate when no capture has it closed.
 */
final class DoorPieces {
    static final int HALF_WIDTH = 2;
    static final int HEIGHT = 6;
    static final int ROOM_DEPTH = 2;
    static final int ORIGIN = CaptureRooms.ORIGIN;
    static final int FLOOR_Y = ExtractRooms.FLOOR_Y;
    static final String CLOSED_CAPTURED = "captured";
    static final String CLOSED_ESTIMATED = "estimated";

    record Closed(Map<Long, String> states, Map<String, String> walls) {
    }

    private DoorPieces() {
    }

    static int turns(RoomCatalogue.Found found) {
        return switch (found.rotation()) {
            case "W" -> 1;
            case "N" -> 2;
            case "E" -> 3;
            default -> 0;
        };
    }

    /** A door of a run seen from one of its rooms: the seam centre and the step from the room out through the door. */
    record Doorway(int seamX, int seamZ, int outX, int outZ) {
        static Doorway of(int[] own, int[] other) {
            int outX = other[0] - own[0], outZ = other[1] - own[1];
            int seamX = outX != 0 ? ORIGIN + 32 * Math.min(own[0], other[0]) + 31 : ORIGIN + 32 * own[0] + ExtractRooms.CENTRE;
            int seamZ = outZ != 0 ? ORIGIN + 32 * Math.min(own[1], other[1]) + 31 : ORIGIN + 32 * own[1] + ExtractRooms.CENTRE;
            return new Doorway(seamX, seamZ, outX, outZ);
        }

        /** The block {@code depth} steps out from the seam (negative: into the room) and {@code along} to its right. */
        int[] at(int depth, int along) {
            return new int[]{seamX + outX * depth - outZ * along, seamZ + outZ * depth + outX * along};
        }
    }

    /**
     * The closed wall for every door of the chosen sighting's run that leads into the room: copied from another
     * sighting of the room where that doorway is shut, else the wall beside the doorway carried across it.
     */
    static Closed closeOwnDoors(CaptureRooms.Seen chosen, List<CaptureRooms.Seen> sources) {
        Map<Long, String> states = new HashMap<>();
        Map<String, String> walls = new HashMap<>();
        for (int[][] door : chosen.run().doors()) {
            boolean first = contains(chosen.cells(), door[0]), second = contains(chosen.cells(), door[1]);
            if (first == second) continue;
            int[] own = first ? door[0] : door[1], other = first ? door[1] : door[0];
            Doorway doorway = Doorway.of(own, other);
            CaptureRooms.Seen source = closedSighting(chosen, doorway, sources);
            for (int depth = 1; depth <= ROOM_DEPTH; depth++) {
                for (int along = -HALF_WIDTH; along <= HALF_WIDTH; along++) {
                    int[] at = doorway.at(-depth, along);
                    for (int y = FLOOR_Y; y < FLOOR_Y + HEIGHT; y++) {
                        String state;
                        if (source != null) {
                            int[] there = mapped(chosen, source, at[0], at[1]);
                            state = BlockStates.rotate(source.run().world().block(there[0], y, there[1]), Math.floorMod(turns(chosen.found()) - turns(source.found()), 4));
                        } else {
                            state = estimate(chosen.run().world(), doorway, depth, along, y);
                        }
                        states.put(AnvilWorld.key(at[0], y, at[1]), state);
                    }
                }
            }
            walls.put(CaptureRooms.doorKey(door), source != null ? CLOSED_CAPTURED : CLOSED_ESTIMATED);
            System.out.println("    " + chosen.name() + " door " + CaptureRooms.doorKey(door) + " in " + chosen.run().id() + ": closed wall "
                    + (source != null ? "from " + source.run().id() : "estimated"));
        }
        return new Closed(states, walls);
    }

    /**
     * A closed wall guessed from the wall beside the doorway: the outer layer continues the blocks on the same side, and
     * the inner layer stays open where either side of it is open, so the doorway becomes a plain recess.
     */
    static String estimate(AnvilWorld world, Doorway doorway, int depth, int along, int y) {
        int[] left = doorway.at(-depth, -HALF_WIDTH - 1), right = doorway.at(-depth, HALF_WIDTH + 1);
        String leftState = world.block(left[0], y, left[1]), rightState = world.block(right[0], y, right[1]);
        String state = along <= 0 ? leftState : rightState;
        if (depth == 1) return BlockStates.passable(state) ? "minecraft:stone_bricks" : state;
        if (y > FLOOR_Y && (BlockStates.passable(leftState) || BlockStates.passable(rightState))) return AnvilWorld.AIR;
        return state;
    }

    private static CaptureRooms.Seen closedSighting(CaptureRooms.Seen chosen, Doorway doorway, List<CaptureRooms.Seen> sources) {
        for (CaptureRooms.Seen source : sources) {
            if (source == chosen) continue;
            int[] seam = mapped(chosen, source, doorway.seamX(), doorway.seamZ());
            int lx = seam[0] - ORIGIN, lz = seam[1] - ORIGIN;
            int[][] door;
            if (Math.floorMod(lx, 32) == 31) {
                door = new int[][]{{Math.floorDiv(lx, 32), Math.floorDiv(lz, 32)}, {Math.floorDiv(lx, 32) + 1, Math.floorDiv(lz, 32)}};
            } else if (Math.floorMod(lz, 32) == 31) {
                door = new int[][]{{Math.floorDiv(lx, 32), Math.floorDiv(lz, 32)}, {Math.floorDiv(lx, 32), Math.floorDiv(lz, 32) + 1}};
            } else {
                continue;
            }
            if (contains(source.cells(), door[0]) == contains(source.cells(), door[1])) continue;
            boolean used = false;
            for (int[][] seen : source.run().doors()) used |= CaptureRooms.doorKey(seen).equals(CaptureRooms.doorKey(door));
            if (!used) return source;
        }
        return null;
    }

    static int[] mapped(CaptureRooms.Seen from, CaptureRooms.Seen to, int x, int z) {
        double[] relative = CaptureRooms.toRelative(from.found(), x, z);
        double[] local = CaptureRooms.toLocal(to.found(), relative[0], relative[1]);
        return new int[]{(int) Math.round(local[0]) + ORIGIN, (int) Math.round(local[1]) + ORIGIN};
    }

    private static boolean contains(int[][] cells, int[] cell) {
        for (int[] own : cells) if (own[0] == cell[0] && own[1] == cell[1]) return true;
        return false;
    }

    /**
     * Every door piece of every run, turned so the door leads south (from the room at negative depth into the one at
     * positive depth), with the 3 by 4 passage through it emptied; identical pieces are kept once.
     */
    static void write(List<CaptureRooms.Run> runs, Path out) throws IOException {
        Map<String, String> pieces = new LinkedHashMap<>();
        Map<String, Integer> seen = new HashMap<>();
        for (CaptureRooms.Run run : runs) {
            for (int[][] door : run.doors()) {
                Doorway doorway = Doorway.of(door[0], door[1]);
                int turns = doorway.outX() != 0 ? 1 : 0;
                String fill = AnvilWorld.bare(run.world().block(doorway.seamX(), FLOOR_Y + 2, doorway.seamZ()));
                String type = switch (fill) {
                    case "minecraft:coal_block" -> "WITHER";
                    case "minecraft:red_terracotta" -> "BLOOD";
                    case "minecraft:infested_chiseled_stone_bricks" -> "ENTRANCE";
                    default -> "PLAIN";
                };
                Map<String, Integer> palette = new LinkedHashMap<>();
                List<Integer> blocks = new ArrayList<>();
                for (int depth = -ROOM_DEPTH; depth <= ROOM_DEPTH; depth++) {
                    for (int along = -HALF_WIDTH; along <= HALF_WIDTH; along++) {
                        for (int y = FLOOR_Y; y < FLOOR_Y + HEIGHT; y++) {
                            boolean roomFloor = Math.abs(depth) == ROOM_DEPTH && y == FLOOR_Y;
                            boolean passage = Math.abs(depth) <= 1 && Math.abs(along) <= 1 && y > FLOOR_Y && y <= FLOOR_Y + 4;
                            if (roomFloor) {
                                blocks.add(-1);
                                continue;
                            }
                            int[] at = doorway.at(depth, along);
                            String state = passage ? AnvilWorld.AIR : BlockStates.rotate(run.world().block(at[0], y, at[1]), turns);
                            blocks.add(palette.computeIfAbsent(state, ignored -> palette.size()));
                        }
                    }
                }
                StringBuilder entry = new StringBuilder("    {\"type\": \"").append(type).append("\", \"palette\": [");
                entry.append(String.join(", ", palette.keySet().stream().map(state -> "\"" + state + "\"").toList()));
                entry.append("],\n     \"blocks\": [").append(String.join(",", blocks.stream().map(String::valueOf).toList())).append("]");
                String body = entry.toString();
                seen.merge(body, 1, Integer::sum);
                pieces.putIfAbsent(body, run.id() + " " + CaptureRooms.doorKey(door));
            }
        }
        StringBuilder json = new StringBuilder("{\n  \"source\": \"Generated by tools/catacombs-rooms/ExtractRooms.java --captures: every door piece of the captured Hypixel runs, ")
                .append("turned so the door leads south. blocks run depth -2..2 (room at negative depth to room at positive depth), then along -2..2 ")
                .append("(the door's right hand facing south, so towards -x), then y 0..5 above the dungeon floor; -1 keeps the room's own floor. ")
                .append("The 3 by 4 passage is emptied. type is the door block the piece was seen with.\",\n  \"pieces\": [\n");
        List<String> entries = new ArrayList<>();
        pieces.forEach((body, from) -> entries.add(body + ", \"seen\": \"" + from + "\", \"count\": " + seen.get(body) + "}"));
        json.append(String.join(",\n", entries)).append("\n  ]\n}\n");
        Files.writeString(out, json.toString(), StandardCharsets.UTF_8);
        System.out.println("door pieces: " + pieces.size() + " distinct from " + seen.values().stream().mapToInt(Integer::intValue).sum() + " doors");
    }
}
