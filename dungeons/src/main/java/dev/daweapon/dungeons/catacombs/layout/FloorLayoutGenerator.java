package dev.daweapon.dungeons.catacombs.layout;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

import java.util.Random;

public final class FloorLayoutGenerator {
    private static final int MAX_ATTEMPTS = 500;
    private static final long ATTEMPT_MIX = 0x9E3779B97F4A7C15L;

    private FloorLayoutGenerator() {
    }

    public static FloorLayout generate(CatacombsFloorDefinition definition, long seed, RoomTemplateLibrary library) {
        return generate(definition.floor(), definition.mode(), seed, library, RoomBudget.fromDefinition(definition));
    }

    public static FloorLayout generate(CatacombsFloor floor, CatacombsMode mode, long seed, RoomTemplateLibrary library) {
        return generate(floor, mode, seed, library, null);
    }

    private static FloorLayout generate(CatacombsFloor floor, CatacombsMode mode, long seed,
                                        RoomTemplateLibrary library, RoomBudget budget) {
        requireTemplates(floor, library);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Random random = new Random(seed + attempt * ATTEMPT_MIX);
            FloorLayout layout = new LayoutAttempt(floor, mode, seed, library, budget, random).build();
            if (layout != null) {
                return layout;
            }
        }
        throw new IllegalStateException("Could not generate a " + floor.displayName() + " layout for seed " + seed);
    }

    private static void requireTemplates(CatacombsFloor floor, RoomTemplateLibrary library) {
        requireKind(library, RoomKind.ENTRANCE, 1);
        requireKind(library, RoomKind.FAIRY, 1);
        requireKind(library, RoomKind.BLOOD, 1);
        requireKind(library, RoomKind.PUZZLE, RoomBudget.MINIMUM_PUZZLES);
        requireKind(library, RoomKind.MINIBOSS, 1);
        if (floor.ordinal() >= RoomBudget.FIRST_TRAP_FLOOR) {
            requireKind(library, RoomKind.TRAP, 1);
        }
        if (!library.has(RoomKind.NORMAL, RoomShape.ONE_BY_ONE)) {
            throw new IllegalStateException("Room template library needs at least one NORMAL ONE_BY_ONE template");
        }
    }

    private static void requireKind(RoomTemplateLibrary library, RoomKind kind, int minimum) {
        if (library.templates(kind).size() < minimum) {
            throw new IllegalStateException("Room template library needs at least " + minimum + " " + kind + " template(s)");
        }
    }
}
