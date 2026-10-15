package dev.daweapon.dungeons.catacombs;

import dev.daweapon.dungeons.catacombs.boss.CatacombsBossEncounter;
import dev.daweapon.dungeons.catacombs.boss.CatacombsBosses;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import dev.daweapon.dungeons.catacombs.run.DungeonRunRules;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Registry of CatacombsFloorDefinitions per mode/floor, with the built-in default Normal and Master configurations.
public record CatacombsRegistry(Map<CatacombsMode, Map<CatacombsFloor, CatacombsFloorDefinition>> floors) {
    private static final int MINIMUM_PUZZLE_ROOMS = 2;

    public CatacombsRegistry {
        floors = Map.copyOf(floors);
    }

    public CatacombsFloorDefinition floor(CatacombsFloor floor, CatacombsMode mode) {
        Map<CatacombsFloor, CatacombsFloorDefinition> byFloor = floors.get(mode);
        if (byFloor == null || !byFloor.containsKey(floor)) {
            throw new IllegalArgumentException("No Catacombs definition for " + mode + " " + floor.displayName());
        }
        return byFloor.get(floor);
    }

    public static CatacombsRegistry defaults() {
        Map<CatacombsMode, Map<CatacombsFloor, CatacombsFloorDefinition>> floors = new EnumMap<>(CatacombsMode.class);
        floors.put(CatacombsMode.NORMAL, floorsFor(CatacombsMode.NORMAL));
        floors.put(CatacombsMode.MASTER, floorsFor(CatacombsMode.MASTER));
        return new CatacombsRegistry(floors);
    }

    private static Map<CatacombsFloor, CatacombsFloorDefinition> floorsFor(CatacombsMode mode) {
        Map<CatacombsFloor, CatacombsFloorDefinition> floors = new EnumMap<>(CatacombsFloor.class);
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            if (floor.supports(mode)) {
                floors.put(floor, definition(floor, mode));
            }
        }
        return floors;
    }

    private static CatacombsBossEncounter bossFor(CatacombsFloor floor, CatacombsMode mode) {
        return switch (floor) {
            case ENTRANCE -> CatacombsBosses.watcher();
            case FLOOR_ONE -> CatacombsBosses.bonzo();
            case FLOOR_TWO -> CatacombsBosses.scarf();
            case FLOOR_THREE -> CatacombsBosses.professor();
            case FLOOR_FOUR -> CatacombsBosses.thorn();
            case FLOOR_FIVE -> CatacombsBosses.livid();
            case FLOOR_SIX -> CatacombsBosses.sadan();
            case FLOOR_SEVEN -> CatacombsBosses.witherLords(mode == CatacombsMode.MASTER);
        };
    }

    private static CatacombsFloorDefinition definition(CatacombsFloor floor, CatacombsMode mode) {
        DungeonRunRules rules = DungeonRunRules.of(floor, mode);
        return new CatacombsFloorDefinition(
                floor,
                mode,
                bossFor(floor, mode),
                rules,
                MINIMUM_PUZZLE_ROOMS,
                floor == CatacombsFloor.ENTRANCE ? 0 : 1,
                rules.trapRoomsEnabled() ? 1 : 0,
                puzzlesFor(floor));
    }

    private static Set<CatacombsPuzzle> puzzlesFor(CatacombsFloor floor) {
        return Arrays.stream(CatacombsPuzzle.values())
                .filter(puzzle -> puzzle.canGenerate(floor))
                .collect(Collectors.toUnmodifiableSet());
    }
}
