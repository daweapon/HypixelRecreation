package dev.daweapon.dungeons.catacombs.puzzle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.daweapon.dungeons.catacombs.puzzle.boulder.BoulderBoard;
import dev.daweapon.dungeons.catacombs.puzzle.creeperbeams.CreeperBeams;
import dev.daweapon.dungeons.catacombs.puzzle.higherorlower.HigherOrLower;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceFillBoard;
import dev.daweapon.dungeons.catacombs.puzzle.icefill.IceTile;
import dev.daweapon.dungeons.catacombs.puzzle.quiz.QuizRound;
import dev.daweapon.dungeons.catacombs.puzzle.teleportmaze.MazePad;
import dev.daweapon.dungeons.catacombs.puzzle.waterboard.WaterBoard;
import dev.daweapon.dungeons.catacombs.puzzle.weirdos.WeirdosCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * The puzzle rooms' captured layout ({@code configuration/skyblock/dungeons/puzzles.json}), keyed by room template.
 * Every position is in the template's own frame and is turned with the room when it is placed.
 */
public final class PuzzleCatalogue {
    public static final Path FILE = Path.of("./configuration/skyblock/dungeons/puzzles.json");

    public record IceFillRoom(List<IceFillBoard> boards, List<RoomBlock> gate, List<RoomBlock> chests) {
    }

    public record Weirdo(RoomSpot spot, RoomBlock chest) {
    }

    public record SkinData(String texture, String signature) {
    }

    public record WeirdosRoom(List<Weirdo> weirdos, List<SkinData> skins, List<String> names, List<WeirdosCase> cases) {
    }

    public record TicTacToeRoom(List<RoomBlock> cells, List<RoomBlock> wall, RoomBlock chest, String hypixelMark, String playerMark) {
    }

    public record TeleportMazeRoom(int y, List<MazePad> pads, RoomSpot exit, RoomBlock chest) {
    }

    public record CreeperBeamsRoom(RoomSpot creeper, CreeperBeams.Space space, int required, List<RoomBlock> lanterns,
                                   List<RoomBlock> pedestal, RoomBlock chest) {
    }

    public record WaterLever(RoomBlock at, String type) {
    }

    public record DoorPart(int y, int z, String facing) {
    }

    public record WaterDoor(String colour, int x) {
    }

    public record WaterBoardRoom(int planeX, WaterBoard.Layout layout, String sourcePlug, List<WaterLever> levers, List<WaterDoor> doors,
                                 List<DoorPart> doorFill, List<DoorPart> doorHeads, List<DoorPart> doorPistons, int closedDoors,
                                 RoomBlock chest) {
    }

    public record HigherOrLowerRoom(List<RoomSpot> blazes, int minHealth, int maxHealth, int level, HigherOrLower.Order order,
                                    int columnX, int columnZ, int bottom, int top) {
    }

    public record BoulderRoom(int gridX, int gridY, int gridZ, int cellSize, int columns, int rows,
                              List<Map<BoulderBoard.Cell, BoulderBoard.Box>> variants, RoomBlock chest) {
    }

    public record QuizPedestal(String option, RoomBlock block) {
    }

    public record QuizRoom(List<QuizRound.Question> questions, int asked, List<QuizPedestal> pedestals, RoomSpot questionSpot) {
    }

    private final Map<String, JsonObject> rooms;

    private PuzzleCatalogue(Map<String, JsonObject> rooms) {
        this.rooms = Map.copyOf(rooms);
    }

    public static PuzzleCatalogue load(Path file) throws IOException {
        return parse(Files.readString(file));
    }

    public static PuzzleCatalogue parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("rooms");
        Map<String, JsonObject> rooms = new HashMap<>();
        root.entrySet().forEach(entry -> rooms.put(entry.getKey(), entry.getValue().getAsJsonObject()));
        return new PuzzleCatalogue(rooms);
    }

    public Optional<IceFillRoom> iceFill(String templateId) {
        return room(templateId, CatacombsPuzzle.ICE_FILL, room -> new IceFillRoom(list(room.getAsJsonArray("boards"), PuzzleCatalogue::board),
                blocks(room.getAsJsonArray("gate")), blocks(room.getAsJsonArray("chests"))));
    }

    public Optional<WeirdosRoom> weirdos(String templateId) {
        return room(templateId, CatacombsPuzzle.THREE_WEIRDOS, room -> new WeirdosRoom(
                list(room.getAsJsonArray("weirdos"), weirdo -> new Weirdo(spot(weirdo.getAsJsonObject()), block(weirdo.getAsJsonObject().get("chest")))),
                list(room.getAsJsonArray("skins"), skin -> new SkinData(skin.getAsJsonObject().get("texture").getAsString(),
                        skin.getAsJsonObject().get("signature").getAsString())),
                list(room.getAsJsonArray("names"), JsonElement::getAsString),
                list(room.getAsJsonArray("cases"), entry -> new WeirdosCase(entry.getAsJsonObject().get("rewarded").getAsInt(),
                        list(entry.getAsJsonObject().getAsJsonArray("lines"), JsonElement::getAsString)))));
    }

    public Optional<TicTacToeRoom> ticTacToe(String templateId) {
        return room(templateId, CatacombsPuzzle.TIC_TAC_TOE, room -> {
            JsonObject marks = room.getAsJsonObject("marks");
            return new TicTacToeRoom(blocks(room.getAsJsonArray("cells")), blocks(room.getAsJsonArray("wall")), block(room.get("chest")),
                    marks.get("hypixel").getAsString(), marks.get("player").getAsString());
        });
    }

    public Optional<TeleportMazeRoom> teleportMaze(String templateId) {
        return room(templateId, CatacombsPuzzle.TELEPORT_MAZE, room -> new TeleportMazeRoom(room.get("y").getAsInt(),
                list(room.getAsJsonArray("pads"), pad -> {
                    JsonObject json = pad.getAsJsonObject();
                    JsonArray land = json.getAsJsonArray("land");
                    return new MazePad(json.get("x").getAsInt(), json.get("z").getAsInt(), MazePad.Kind.valueOf(json.get("kind").getAsString()),
                            json.get("room").getAsInt(), land.get(0).getAsDouble(), land.get(1).getAsDouble());
                }),
                spot(room.getAsJsonObject("exit")), block(room.get("chest"))));
    }

    public Optional<CreeperBeamsRoom> creeperBeams(String templateId) {
        return room(templateId, CatacombsPuzzle.CREEPER_BEAMS, room -> {
            JsonObject space = room.getAsJsonObject("space");
            return new CreeperBeamsRoom(spot(room.getAsJsonObject("creeper")),
                    new CreeperBeams.Space(space.get("minX").getAsDouble(), space.get("minY").getAsDouble(), space.get("minZ").getAsDouble(),
                            space.get("maxX").getAsDouble(), space.get("maxY").getAsDouble(), space.get("maxZ").getAsDouble()),
                    room.get("required").getAsInt(), blocks(room.getAsJsonArray("lanterns")), blocks(room.getAsJsonArray("pedestal")),
                    block(room.get("chest")));
        });
    }

    public Optional<WaterBoardRoom> waterBoard(String templateId) {
        return room(templateId, CatacombsPuzzle.WATER_BOARD, room -> {
            JsonObject plane = room.getAsJsonObject("plane");
            JsonObject source = room.getAsJsonObject("source");
            JsonObject doorway = room.getAsJsonObject("doorway");
            WaterBoard.Layout layout = new WaterBoard.Layout(plane.get("minZ").getAsInt(), plane.get("maxY").getAsInt(),
                    list(room.getAsJsonArray("rows"), JsonElement::getAsString),
                    list(room.getAsJsonArray("gates"), gate -> {
                        JsonObject json = gate.getAsJsonObject();
                        return new WaterBoard.Gate(json.get("type").getAsString(), json.get("z").getAsInt(), json.get("y").getAsInt(),
                                json.get("extended").getAsBoolean());
                    }),
                    source.get("z").getAsInt(), source.get("y").getAsInt(),
                    list(room.getAsJsonArray("holes"), hole -> new WaterBoard.Hole(hole.getAsJsonObject().get("z").getAsInt(),
                            hole.getAsJsonObject().get("door").getAsString())));
            return new WaterBoardRoom(plane.get("x").getAsInt(), layout, source.get("plug").getAsString(),
                    list(room.getAsJsonArray("levers"), lever -> new WaterLever(block(lever.getAsJsonObject().get("at")),
                            lever.getAsJsonObject().get("type").getAsString())),
                    list(room.getAsJsonArray("doors"), door -> new WaterDoor(door.getAsJsonObject().get("colour").getAsString(),
                            door.getAsJsonObject().get("x").getAsInt())),
                    doorParts(doorway.getAsJsonArray("fill")), doorParts(doorway.getAsJsonArray("heads")),
                    doorParts(doorway.getAsJsonArray("pistons")), room.get("closedDoors").getAsInt(), block(room.get("chest")));
        });
    }

    public Optional<HigherOrLowerRoom> higherOrLower(String templateId) {
        return room(templateId, CatacombsPuzzle.HIGHER_OR_LOWER, room -> {
            JsonObject health = room.getAsJsonObject("health");
            JsonObject column = room.getAsJsonObject("column");
            return new HigherOrLowerRoom(list(room.getAsJsonArray("blazes"), spot -> {
                JsonArray at = spot.getAsJsonArray();
                return new RoomSpot(at.get(0).getAsDouble(), at.get(1).getAsDouble(), at.get(2).getAsDouble(), 0);
            }), health.get("min").getAsInt(), health.get("max").getAsInt(), room.get("level").getAsInt(),
                    HigherOrLower.Order.valueOf(room.get("order").getAsString()), column.get("x").getAsInt(), column.get("z").getAsInt(),
                    column.get("bottom").getAsInt(), column.get("top").getAsInt());
        });
    }

    public Optional<BoulderRoom> boulder(String templateId) {
        return room(templateId, CatacombsPuzzle.BOULDER, room -> {
            JsonObject grid = room.getAsJsonObject("grid");
            return new BoulderRoom(grid.get("x").getAsInt(), grid.get("y").getAsInt(), grid.get("z").getAsInt(), grid.get("cell").getAsInt(),
                    grid.get("columns").getAsInt(), grid.get("rows").getAsInt(),
                    list(room.getAsJsonArray("variants"), variant -> {
                        Map<BoulderBoard.Cell, BoulderBoard.Box> boxes = new LinkedHashMap<>();
                        variant.getAsJsonObject().getAsJsonArray("boxes").forEach(box -> {
                            JsonObject json = box.getAsJsonObject();
                            Set<BoulderBoard.Face> buttons = new HashSet<>(list(json.getAsJsonArray("buttons"),
                                    face -> BoulderBoard.Face.valueOf(face.getAsString().toUpperCase(Locale.ROOT))));
                            boxes.put(new BoulderBoard.Cell(json.get("column").getAsInt(), json.get("row").getAsInt()),
                                    new BoulderBoard.Box(json.get("wood").getAsString(), buttons));
                        });
                        return Map.copyOf(boxes);
                    }), block(room.get("chest")));
        });
    }

    public Optional<QuizRoom> quiz(String templateId) {
        return room(templateId, CatacombsPuzzle.QUIZ, room -> new QuizRoom(
                list(room.getAsJsonArray("questions"), question -> {
                    JsonObject json = question.getAsJsonObject();
                    return new QuizRound.Question(json.get("question").getAsString(), json.get("answer").getAsString(),
                            list(json.getAsJsonArray("wrong"), JsonElement::getAsString));
                }),
                room.get("asked").getAsInt(),
                list(room.getAsJsonArray("pedestals"), pedestal -> new QuizPedestal(pedestal.getAsJsonObject().get("option").getAsString(),
                        block(pedestal.getAsJsonObject().get("block")))),
                spot(room.getAsJsonObject("question"))));
    }

    private static List<DoorPart> doorParts(JsonArray array) {
        return list(array, part -> {
            JsonArray at = part.getAsJsonArray();
            return new DoorPart(at.get(0).getAsInt(), at.get(1).getAsInt(), at.size() > 2 ? at.get(2).getAsString() : null);
        });
    }

    public boolean has(String templateId) {
        return rooms.containsKey(templateId);
    }

    /** The template's room data read as the given puzzle, or empty when the template holds another puzzle or none. */
    private <T> Optional<T> room(String templateId, CatacombsPuzzle puzzle, Function<JsonObject, T> reader) {
        JsonObject room = rooms.get(templateId);
        if (room == null || !room.has("puzzle") || !puzzle.name().equals(room.get("puzzle").getAsString())) return Optional.empty();
        return Optional.of(reader.apply(room));
    }

    private static IceFillBoard board(JsonElement element) {
        JsonObject board = element.getAsJsonObject();
        Set<IceTile> tiles = new HashSet<>(list(board.getAsJsonArray("tiles"), PuzzleCatalogue::tile));
        return new IceFillBoard(board.get("y").getAsInt(), tiles, tile(board.get("start")), tile(board.get("exit")));
    }

    private static IceTile tile(JsonElement element) {
        JsonArray at = element.getAsJsonArray();
        return new IceTile(at.get(0).getAsInt(), at.get(1).getAsInt());
    }

    private static RoomBlock block(JsonElement element) {
        JsonArray at = element.getAsJsonArray();
        return new RoomBlock(at.get(0).getAsInt(), at.get(1).getAsInt(), at.get(2).getAsInt());
    }

    private static List<RoomBlock> blocks(JsonArray array) {
        return list(array, PuzzleCatalogue::block);
    }

    private static RoomSpot spot(JsonObject json) {
        return new RoomSpot(json.get("x").getAsDouble(), json.get("y").getAsDouble(), json.get("z").getAsDouble(),
                json.has("yaw") ? json.get("yaw").getAsFloat() : 0f);
    }

    private static <T> List<T> list(JsonArray array, Function<JsonElement, T> reader) {
        List<T> values = new ArrayList<>();
        array.forEach(element -> values.add(reader.apply(element)));
        return List.copyOf(values);
    }
}
