package dev.daweapon.type.catacombs.arena;

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
import java.util.Optional;

/**
 * Captured spots inside particular real rooms ({@code configuration/skyblock/dungeons/room_features.json}): where Mort
 * stands in the Entrance, Tomioka's ledge, the Watcher's perch and flight stops and the boss portal in the Blood room, and the space the
 * Fairy Room's fairies drift in. Spots are in the template's own frame and are turned with the room when placed.
 */
public final class RoomFeatures {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/room_features.json");
    private static volatile RoomFeatures shared;

    public record Spot(double x, double y, double z, float yaw) {
    }

    public record Box(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
    }

    public record PortalFrame(int minX, int maxX, int minY, int maxY, int z) {
    }

    private record Features(Spot mort, Spot watcher, List<Spot> watcherStops, List<Spot> blessings, PortalFrame portal, Box fairies,
                            Spot tomioka) {
    }

    private final Map<String, Features> rooms;

    private RoomFeatures(Map<String, Features> rooms) {
        this.rooms = rooms;
    }

    public static RoomFeatures shared() {
        RoomFeatures loaded = shared;
        if (loaded != null) return loaded;
        synchronized (RoomFeatures.class) {
            if (shared == null) shared = load(FILE);
            return shared;
        }
    }

    public static RoomFeatures load(Path file) {
        try {
            return parse(Files.readString(file));
        } catch (IOException | RuntimeException exception) {
            Logger.error(exception, "Could not read the Catacombs room features from {}", file);
            return new RoomFeatures(Map.of());
        }
    }

    static RoomFeatures parse(String json) {
        Map<String, Features> rooms = new HashMap<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("rooms");
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            JsonObject room = entry.getValue().getAsJsonObject();
            rooms.put(entry.getKey(), new Features(
                    room.has("mort") ? spot(room.getAsJsonObject("mort")) : null,
                    room.has("watcher") ? spot(room.getAsJsonObject("watcher")) : null,
                    spots(room, "watcherStops"),
                    spots(room, "blessings"),
                    room.has("portal") ? portal(room.getAsJsonObject("portal")) : null,
                    room.has("fairies") ? box(room.getAsJsonObject("fairies")) : null,
                    room.has("tomioka") ? spot(room.getAsJsonObject("tomioka")) : null));
        }
        return new RoomFeatures(Map.copyOf(rooms));
    }

    private static List<Spot> spots(JsonObject room, String key) {
        List<Spot> spots = new ArrayList<>();
        if (room.has(key)) room.getAsJsonArray(key).forEach(spot -> spots.add(spot(spot.getAsJsonObject())));
        return List.copyOf(spots);
    }

    private static Spot spot(JsonObject json) {
        return new Spot(json.get("x").getAsDouble(), json.get("y").getAsDouble(), json.get("z").getAsDouble(),
                json.has("yaw") ? json.get("yaw").getAsFloat() : 0);
    }

    private static PortalFrame portal(JsonObject json) {
        return new PortalFrame(json.get("minX").getAsInt(), json.get("maxX").getAsInt(), json.get("minY").getAsInt(),
                json.get("maxY").getAsInt(), json.get("z").getAsInt());
    }

    private static Box box(JsonObject json) {
        return new Box(json.get("minX").getAsDouble(), json.get("maxX").getAsDouble(), json.get("minY").getAsDouble(),
                json.get("maxY").getAsDouble(), json.get("minZ").getAsDouble(), json.get("maxZ").getAsDouble());
    }

    private Optional<Features> of(PlacedRoom room) {
        return Optional.ofNullable(rooms.get(room.template().id()));
    }

    public Optional<Spot> mort(PlacedRoom room) {
        return of(room).map(Features::mort).map(spot -> place(room, spot));
    }

    public Optional<Spot> tomioka(PlacedRoom room) {
        return of(room).map(Features::tomioka).map(spot -> place(room, spot));
    }

    public Optional<Spot> watcher(PlacedRoom room) {
        return of(room).map(Features::watcher).map(spot -> place(room, spot));
    }

    public List<Spot> watcherStops(PlacedRoom room) {
        return of(room).map(features -> features.watcherStops().stream().map(stop -> place(room, stop)).toList()).orElse(List.of());
    }

    public List<Spot> blessings(PlacedRoom room) {
        return of(room).map(features -> features.blessings().stream().map(spot -> place(room, spot)).toList()).orElse(List.of());
    }

    /** The portal's blocks in world space, or empty when the room has no captured frame. */
    public List<int[]> portal(PlacedRoom room) {
        PortalFrame frame = of(room).map(Features::portal).orElse(null);
        if (frame == null) return List.of();
        List<int[]> blocks = new ArrayList<>();
        for (int x = frame.minX(); x <= frame.maxX(); x++) {
            for (int y = frame.minY(); y <= frame.maxY(); y++) {
                Spot turned = place(room, new Spot(x + 0.5, y, frame.z() + 0.5, 0));
                blocks.add(new int[]{(int) Math.floor(turned.x()), (int) Math.floor(turned.y()), (int) Math.floor(turned.z())});
            }
        }
        return blocks;
    }

    /** A captured frame spans the template's X axis, so a room turned an odd number of times has its portal along Z. */
    public static boolean portalAlongX(PlacedRoom room) {
        return room.rotation().quarterTurns() % 2 == 0;
    }

    public Optional<Box> fairies(PlacedRoom room) {
        return of(room).map(Features::fairies).map(box -> {
            Spot first = place(room, new Spot(box.minX(), box.minY(), box.minZ(), 0));
            Spot second = place(room, new Spot(box.maxX(), box.maxY(), box.maxZ(), 0));
            return new Box(Math.min(first.x(), second.x()), Math.max(first.x(), second.x()), first.y(), second.y(),
                    Math.min(first.z(), second.z()), Math.max(first.z(), second.z()));
        });
    }

    public static Spot place(PlacedRoom room, Spot local) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        CellGeometry.PlanarPoint turned = CellGeometry.rotateLocal(local.x(), local.z(), room.shape(), room.rotation());
        float yaw = local.yaw() + 90f * room.rotation().quarterTurns();
        return new Spot(origin.x() + turned.x(), origin.y() + local.y(), origin.z() + turned.z(), yaw);
    }
}
