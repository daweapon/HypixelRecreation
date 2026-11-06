package dev.daweapon.type.catacombs.arena;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.type.skyblockgeneric.schematic.SchematicRotation;
import net.minestom.server.instance.block.Block;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Hypixel's doorways, captured from real runs ({@code configuration/skyblock/dungeons/door_pieces.json}). Every
 * door pastes one piece over the seam and both rooms' two outer layers: a frame 5 wide and 6 tall with the 3 by 4
 * passage through it, and an arch on each room's inner face. Hypixel's pieces differ per door, so each door of a layout
 * takes one at random (fixed by the layout's seed), from those seen with the same door block where there are any.
 */
public final class DoorPieces {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/door_pieces.json");
    public static final int REACH = 2;
    public static final int HEIGHT = 6;
    private static final int SIDE = 2 * REACH + 1;
    private static volatile DoorPieces shared;

    /** One piece, turned so its door leads south; null blocks keep what the room has there. */
    public record Piece(String type, Block[] blocks) {
        public Block at(int depth, int along, int y) {
            return blocks[((depth + REACH) * SIDE + along + REACH) * HEIGHT + y];
        }
    }

    private final List<Piece> pieces;

    private DoorPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }

    public static DoorPieces shared() {
        DoorPieces loaded = shared;
        if (loaded != null) return loaded;
        synchronized (DoorPieces.class) {
            if (shared == null) shared = load(FILE);
            return shared;
        }
    }

    public static DoorPieces load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not read the Catacombs door pieces from {}", file);
            return new DoorPieces(List.of());
        }
    }

    static DoorPieces parse(String json) {
        List<Piece> pieces = new ArrayList<>();
        for (JsonElement element : JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("pieces")) {
            JsonObject piece = element.getAsJsonObject();
            List<Block> palette = new ArrayList<>();
            piece.getAsJsonArray("palette").forEach(state -> palette.add(Block.fromState(state.getAsString())));
            var indices = piece.getAsJsonArray("blocks");
            Block[] blocks = new Block[indices.size()];
            for (int i = 0; i < blocks.length; i++) {
                int index = indices.get(i).getAsInt();
                blocks[i] = index < 0 ? null : palette.get(index);
            }
            pieces.add(new Piece(piece.get("type").getAsString(), blocks));
        }
        return new DoorPieces(List.copyOf(pieces));
    }

    public List<Piece> pieces() {
        return pieces;
    }

    /** The door's appearance as Hypixel names it: Wither, Blood, the Entrance's gate, or a plain doorway. */
    static String type(LayoutDoor door, int startRoom) {
        return switch (door.type()) {
            case WITHER -> "WITHER";
            case BLOOD -> "BLOOD";
            case PLAIN -> door.connects(startRoom) ? "ENTRANCE" : "PLAIN";
        };
    }

    /** The piece a layout's door gets. */
    public Piece pick(FloorLayout layout, LayoutDoor door) {
        String type = type(door, layout.startRoom().index());
        List<Piece> pool = pieces.stream().filter(piece -> piece.type().equals(type)).toList();
        if (pool.isEmpty()) pool = pieces.stream().filter(piece -> piece.type().equals("PLAIN")).toList();
        if (pool.isEmpty()) pool = pieces;
        Cell low = low(door);
        Random random = new Random(layout.seed() * 31 + low.col() * 977L + low.row() * 31L + (eastWest(door) ? 1 : 0));
        return pool.get(random.nextInt(pool.size()));
    }

    /** Every block the layout's doors put in the world, by packed position. */
    public Map<Long, Block> blocks(FloorLayout layout) {
        Map<Long, Block> placed = new HashMap<>();
        if (pieces.isEmpty()) return placed;
        for (LayoutDoor door : layout.doors()) {
            Piece piece = pick(layout, door);
            CellGeometry.BlockPosition seam = CellGeometry.doorPosition(door.from(), door.to());
            boolean eastWest = eastWest(door);
            int outX = eastWest ? 1 : 0, outZ = eastWest ? 0 : 1;
            int turns = eastWest ? 3 : 0;
            for (int depth = -REACH; depth <= REACH; depth++) {
                for (int along = -REACH; along <= REACH; along++) {
                    for (int y = 0; y < HEIGHT; y++) {
                        Block block = piece.at(depth, along, y);
                        if (block == null) continue;
                        int x = seam.x() + outX * depth - outZ * along;
                        int z = seam.z() + outZ * depth + outX * along;
                        placed.put(CatacombsArenaBlocks.pack(x, CellGeometry.FLOOR_Y + y, z), SchematicRotation.rotate(block, turns));
                    }
                }
            }
        }
        return placed;
    }

    static boolean eastWest(LayoutDoor door) {
        return door.from().row() == door.to().row();
    }

    static Cell low(LayoutDoor door) {
        return door.from().col() + door.from().row() <= door.to().col() + door.to().row() ? door.from() : door.to();
    }
}
