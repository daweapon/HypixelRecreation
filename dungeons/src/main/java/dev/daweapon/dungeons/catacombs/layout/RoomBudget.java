package dev.daweapon.dungeons.catacombs.layout;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsFloorDefinition;

import java.util.Random;

record RoomBudget(int puzzles, int minibosses, int traps) {
    static final int MINIMUM_PUZZLES = 2;
    static final int FIRST_TRAP_FLOOR = 3;

    static RoomBudget forFloor(CatacombsFloor floor, Random random) {
        int number = floor.ordinal();
        int puzzles = switch (number) {
            case 0, 1 -> MINIMUM_PUZZLES;
            case 2, 3 -> MINIMUM_PUZZLES + random.nextInt(2);
            case 4, 5 -> 3;
            default -> 3 + random.nextInt(2);
        };
        return new RoomBudget(puzzles, 1, trapsFor(floor, 1));
    }

    static RoomBudget fromDefinition(CatacombsFloorDefinition definition) {
        return new RoomBudget(
                Math.max(MINIMUM_PUZZLES, definition.puzzleRooms()),
                Math.min(1, definition.minibossRooms()),
                trapsFor(definition.floor(), Math.min(1, definition.trapRooms())));
    }

    private static int trapsFor(CatacombsFloor floor, int requested) {
        return floor.ordinal() >= FIRST_TRAP_FLOOR ? requested : 0;
    }
}
