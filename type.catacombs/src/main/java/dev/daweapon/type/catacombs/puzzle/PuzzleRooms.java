package dev.daweapon.type.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.puzzle.PuzzleCatalogue;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** The run's playable puzzle rooms: one {@link PuzzleRoom} per real puzzle room the catalogue has data for. */
public final class PuzzleRooms {
    @FunctionalInterface
    private interface Factory {
        Optional<PuzzleRoom> create(PuzzleContext context, PlacedRoom room, PuzzleCatalogue catalogue, String templateId);
    }

    private static final List<Factory> FACTORIES = List.of(
            (context, room, catalogue, id) -> catalogue.iceFill(id).map(data -> new IceFillRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.weirdos(id).map(data -> new ThreeWeirdosRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.ticTacToe(id).map(data -> new TicTacToeRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.teleportMaze(id).map(data -> new TeleportMazeRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.creeperBeams(id).map(data -> new CreeperBeamsRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.waterBoard(id).map(data -> new WaterBoardRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.higherOrLower(id).map(data -> new HigherOrLowerRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.boulder(id).map(data -> new BoulderRoom(context, room, data)),
            (context, room, catalogue, id) -> catalogue.quiz(id).map(data -> new QuizRoom(context, room, data)));

    private static volatile PuzzleCatalogue shared;

    private final PuzzleContext context;
    private final Map<Integer, PuzzleRoom> rooms = new ConcurrentHashMap<>();
    private final Map<Integer, PuzzleRoom> ready = new ConcurrentHashMap<>();
    private volatile boolean disposed;

    public PuzzleRooms(PuzzleContext context, FloorLayout layout, Predicate<PlacedRoom> real) {
        this.context = context;
        PuzzleCatalogue catalogue = catalogue();
        for (PlacedRoom room : layout.roomsOfKind(RoomKind.PUZZLE)) {
            if (!real.test(room)) continue;
            try {
                create(context, room, catalogue).ifPresent(puzzle -> rooms.put(room.index(), puzzle));
            } catch (RuntimeException exception) {
                Logger.error(exception, "Could not set up the puzzle in room {} ({})", room.index(), room.template().id());
            }
        }
    }

    public static PuzzleCatalogue catalogue() {
        PuzzleCatalogue loaded = shared;
        if (loaded != null) return loaded;
        synchronized (PuzzleRooms.class) {
            if (shared == null) {
                try {
                    shared = PuzzleCatalogue.load(PuzzleCatalogue.FILE);
                } catch (IOException | RuntimeException exception) {
                    Logger.error(exception, "Could not read the Catacombs puzzles from {}", PuzzleCatalogue.FILE);
                    shared = PuzzleCatalogue.parse("{\"rooms\":{}}");
                }
            }
            return shared;
        }
    }

    static Optional<PuzzleRoom> create(PuzzleContext context, PlacedRoom room, PuzzleCatalogue catalogue) {
        String id = room.template().id();
        return FACTORIES.stream().map(factory -> factory.create(context, room, catalogue, id)).flatMap(Optional::stream).findFirst();
    }

    public void start() {
        for (PuzzleRoom puzzle : rooms.values()) {
            loadRoom(puzzle.room()).thenRun(() -> {
                if (disposed) return;
                try {
                    puzzle.start();
                    ready.put(puzzle.room().index(), puzzle);
                } catch (RuntimeException exception) {
                    Logger.error(exception, "Could not start the {} puzzle in room {}", puzzle.puzzle().displayName(), puzzle.room().index());
                }
            });
        }
    }

    private CompletableFuture<Void> loadRoom(PlacedRoom room) {
        CellGeometry.BlockPosition origin = CellGeometry.blockOrigin(room.origin());
        int width = CellGeometry.widthBlocks(room.shape(), room.rotation());
        int depth = CellGeometry.depthBlocks(room.shape(), room.rotation());
        List<CompletableFuture<?>> chunks = new ArrayList<>();
        for (int chunkX = origin.x() >> 4; chunkX <= (origin.x() + width) >> 4; chunkX++) {
            for (int chunkZ = origin.z() >> 4; chunkZ <= (origin.z() + depth) >> 4; chunkZ++) {
                chunks.add(context.host().instance().loadChunk(chunkX, chunkZ));
            }
        }
        return CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new));
    }

    public Optional<PuzzleRoom> in(PlacedRoom room) {
        return Optional.ofNullable(ready.get(room.index()));
    }

    public boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        PuzzleRoom puzzle = at(x, z);
        return puzzle != null && puzzle.interact(player, x, y, z);
    }

    public boolean interactEntity(SkyBlockPlayer player, Entity entity) {
        PuzzleRoom puzzle = at(entity.getPosition().blockX(), entity.getPosition().blockZ());
        return puzzle != null && puzzle.interactEntity(player, entity);
    }

    public void moved(SkyBlockPlayer player, Pos to, boolean onGround) {
        PuzzleRoom puzzle = at(to.blockX(), to.blockZ());
        if (puzzle != null) puzzle.moved(player, to, onGround);
    }

    public boolean arrowHit(SkyBlockPlayer player, int x, int y, int z) {
        PuzzleRoom puzzle = at(x, z);
        return puzzle != null && puzzle.arrowHit(player, x, y, z);
    }

    private @Nullable PuzzleRoom at(int x, int z) {
        if (ready.isEmpty()) return null;
        return context.host().arena().plan().roomAtColumn(x, z).map(room -> ready.get(room.index())).orElse(null);
    }

    public void poll() {
        ready.values().forEach(PuzzleRoom::poll);
    }

    public void dispose() {
        disposed = true;
        ready.values().forEach(PuzzleRoom::dispose);
        ready.clear();
        rooms.clear();
    }
}
