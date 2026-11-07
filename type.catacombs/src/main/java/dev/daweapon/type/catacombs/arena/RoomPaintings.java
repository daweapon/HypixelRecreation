package dev.daweapon.type.catacombs.arena;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import net.minestom.server.utils.Direction;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Captured paintings of real rooms ({@code "paintings"} in {@code room_features.json}). Paintings are entities, so the
 * room schematics do not carry them. Each one is stored like the server stores a painting: the block it hangs in front
 * of its wall (the bottom block of its centre column, vanilla's anchor), the way it faces, its variant and size.
 */
public final class RoomPaintings {
    private static final List<Direction> CLOCKWISE = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static volatile RoomPaintings shared;

    public record Painting(int x, int y, int z, Direction facing, String variant, int width, int height) {
        /** The blocks the painting covers; even sizes grow counter-clockwise of its facing and upward from the anchor, as in vanilla. */
        public List<int[]> covered() {
            Direction along = turn(facing, 3);
            int first = -((width - 1) / 2), bottom = -((height - 1) / 2);
            List<int[]> blocks = new ArrayList<>();
            for (int across = first; across < first + width; across++) {
                for (int up = bottom; up < bottom + height; up++) {
                    blocks.add(new int[]{x + along.normalX() * across, y + up, z + along.normalZ() * across});
                }
            }
            return blocks;
        }
    }

    private final Map<String, List<Painting>> rooms;

    private RoomPaintings(Map<String, List<Painting>> rooms) {
        this.rooms = rooms;
    }

    public static RoomPaintings shared() {
        RoomPaintings loaded = shared;
        if (loaded != null) return loaded;
        synchronized (RoomPaintings.class) {
            if (shared == null) shared = load(RoomFeatures.FILE);
            return shared;
        }
    }

    public static RoomPaintings load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not read the Catacombs room paintings from {}", file);
            return new RoomPaintings(Map.of());
        }
    }

    static RoomPaintings parse(String json) {
        Map<String, List<Painting>> rooms = new HashMap<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("rooms");
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject room = entry.getValue().getAsJsonObject();
            if (!room.has("paintings")) continue;
            List<Painting> paintings = new ArrayList<>();
            room.getAsJsonArray("paintings").forEach(element -> {
                JsonObject painting = element.getAsJsonObject();
                paintings.add(new Painting(painting.get("x").getAsInt(), painting.get("y").getAsInt(), painting.get("z").getAsInt(),
                        Direction.valueOf(painting.get("facing").getAsString()), painting.get("variant").getAsString(),
                        painting.get("width").getAsInt(), painting.get("height").getAsInt()));
            });
            rooms.put(entry.getKey(), List.copyOf(paintings));
        }
        return new RoomPaintings(Map.copyOf(rooms));
    }

    public Set<String> templateIds() {
        return rooms.keySet();
    }

    /** The room's paintings in world space, turned with the room. */
    public List<Painting> of(PlacedRoom room) {
        return rooms.getOrDefault(room.template().id(), List.of()).stream().map(painting -> place(room, painting)).toList();
    }

    public static Painting place(PlacedRoom room, Painting local) {
        RoomFeatures.Spot turned = RoomFeatures.place(room, new RoomFeatures.Spot(local.x() + 0.5, local.y(), local.z() + 0.5, 0));
        return new Painting((int) Math.floor(turned.x()), (int) Math.floor(turned.y()), (int) Math.floor(turned.z()),
                turn(local.facing(), room.rotation().quarterTurns()), local.variant(), local.width(), local.height());
    }

    private static Direction turn(Direction facing, int quarterTurns) {
        return CLOCKWISE.get(Math.floorMod(CLOCKWISE.indexOf(facing) + quarterTurns, CLOCKWISE.size()));
    }
}
