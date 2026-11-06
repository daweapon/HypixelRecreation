package dev.daweapon.type.catacombs.arena;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The interactive secret blocks of the real rooms ({@code configuration/skyblock/dungeons/room_mechanisms.json},
 * written by {@code tools/catacombs-rooms}): which blocks each lever removes and the cracked stone brick plug of each
 * Superboom wall. Positions are in the template's own frame and are turned with the room when placed.
 */
public final class RoomMechanisms {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/room_mechanisms.json");
    private static volatile RoomMechanisms shared;

    public record Lever(String label, int[] at, List<int[]> opens, String source, List<String> secrets, List<int[]> secretBlocks,
                        boolean freesSecret) {
    }

    public record Wall(String label, List<int[]> blocks) {
    }

    private record Room(List<Lever> levers, List<Wall> walls) {
    }

    private final Map<String, Room> rooms;

    private RoomMechanisms(Map<String, Room> rooms) {
        this.rooms = rooms;
    }

    public static RoomMechanisms shared() {
        RoomMechanisms loaded = shared;
        if (loaded != null) return loaded;
        synchronized (RoomMechanisms.class) {
            if (shared == null) shared = load(FILE);
            return shared;
        }
    }

    public static RoomMechanisms load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not read the Catacombs room mechanisms from {}", file);
            return new RoomMechanisms(Map.of());
        }
    }

    static RoomMechanisms parse(String json) {
        Map<String, Room> rooms = new HashMap<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("rooms");
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject room = entry.getValue().getAsJsonObject();
            List<Lever> levers = new ArrayList<>();
            if (room.has("levers")) {
                for (JsonElement element : room.getAsJsonArray("levers")) {
                    JsonObject lever = element.getAsJsonObject();
                    List<String> secrets = new ArrayList<>();
                    lever.getAsJsonArray("secrets").forEach(secret -> secrets.add(secret.getAsString()));
                    levers.add(new Lever(lever.get("label").getAsString(),
                            new int[]{lever.get("x").getAsInt(), lever.get("y").getAsInt(), lever.get("z").getAsInt()},
                            blocks(lever.getAsJsonArray("opens")), lever.get("source").getAsString(), List.copyOf(secrets),
                            lever.has("secretBlocks") ? blocks(lever.getAsJsonArray("secretBlocks")) : List.of(),
                            lever.has("freesSecret") && lever.get("freesSecret").getAsBoolean()));
                }
            }
            List<Wall> walls = new ArrayList<>();
            if (room.has("superbooms")) {
                for (JsonElement element : room.getAsJsonArray("superbooms")) {
                    JsonObject wall = element.getAsJsonObject();
                    walls.add(new Wall(wall.get("label").getAsString(), blocks(wall.getAsJsonArray("blocks"))));
                }
            }
            rooms.put(entry.getKey(), new Room(List.copyOf(levers), List.copyOf(walls)));
        }
        return new RoomMechanisms(Map.copyOf(rooms));
    }

    private static List<int[]> blocks(JsonArray array) {
        List<int[]> blocks = new ArrayList<>();
        for (JsonElement element : array) {
            JsonArray at = element.getAsJsonArray();
            blocks.add(new int[]{at.get(0).getAsInt(), at.get(1).getAsInt(), at.get(2).getAsInt()});
        }
        return List.copyOf(blocks);
    }

    /** The template's levers, unturned. */
    public List<Lever> levers(String templateId) {
        Room room = rooms.get(templateId);
        return room == null ? List.of() : room.levers();
    }

    /** The template's Superboom walls, unturned. */
    public List<Wall> walls(String templateId) {
        Room room = rooms.get(templateId);
        return room == null ? List.of() : room.walls();
    }

    /** The room's levers with every position turned and moved into world space. */
    public List<Lever> levers(PlacedRoom room) {
        return levers(room.template().id()).stream()
                .map(lever -> new Lever(lever.label(), place(room, lever.at()), place(room, lever.opens()), lever.source(), lever.secrets(),
                        place(room, lever.secretBlocks()), lever.freesSecret()))
                .toList();
    }

    /** The room's Superboom walls in world space. */
    public List<Wall> walls(PlacedRoom room) {
        return walls(room.template().id()).stream().map(wall -> new Wall(wall.label(), place(room, wall.blocks()))).toList();
    }

    private static List<int[]> place(PlacedRoom room, List<int[]> blocks) {
        return blocks.stream().map(at -> place(room, at)).toList();
    }

    /** A template-frame block (y above the dungeon floor) in world space. */
    public static int[] place(PlacedRoom room, int[] local) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        CellGeometry.PlanarPoint turned = CellGeometry.rotateLocal(local[0] + 0.5, local[2] + 0.5, room.shape(), room.rotation());
        return new int[]{origin.x() + (int) Math.floor(turned.x()), CellGeometry.FLOOR_Y + local[1], origin.z() + (int) Math.floor(turned.z())};
    }
}
