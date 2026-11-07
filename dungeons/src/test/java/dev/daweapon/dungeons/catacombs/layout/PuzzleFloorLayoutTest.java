package dev.daweapon.dungeons.catacombs.layout;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.puzzle.CatacombsPuzzle;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PuzzleFloorLayoutTest {
    private final RoomTemplateLibrary library = PlaceholderLibrary.load();

    @Test
    void everyPlacedPuzzleIsAllowedOnItsFloor() {
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            for (long seed = 0; seed < 25; seed++) {
                FloorLayout layout = FloorLayoutGenerator.generate(floor, CatacombsMode.NORMAL, seed, library);
                for (PlacedRoom room : layout.roomsOfKind(RoomKind.PUZZLE)) {
                    assertTrue(CatacombsPuzzle.templateAllowedOn(room.template().id(), floor),
                            room.template().id() + " on " + floor + " seed " + seed);
                }
            }
        }
    }

    @Test
    void floorSevenCanStillDealIceFill() {
        Set<String> seen = new HashSet<>();
        for (long seed = 0; seed < 200 && !seen.contains("ice_fill"); seed++) {
            FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, seed, library)
                    .roomsOfKind(RoomKind.PUZZLE).forEach(room -> seen.add(room.template().id()));
        }
        assertTrue(seen.contains("ice_fill"), seen.toString());
    }
}
