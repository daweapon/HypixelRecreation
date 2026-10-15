package dev.daweapon.dungeons.catacombs.layout;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomTemplateLibraryTest {
    private static String template(String overrides) {
        String base = """
                {"id": "t1", "name": "T1", "shape": "ONE_BY_ONE", "kind": "NORMAL", "secretCount": 1,
                 "doorSockets": [{"col": 0, "row": 0, "side": "NORTH"}],
                 "mobSpawns": [{"id": "a", "x": 5, "y": 1, "z": 5, "mobId": "ZOMBIE_GRUNT", "starred": true}],
                 "secretAnchors": [{"type": "CHEST", "x": 3, "y": 1, "z": 3}]}
                """;
        if (overrides.isEmpty()) {
            return base;
        }
        String[] change = overrides.split("=>", -1);
        return base.replace(change[0], change[1]);
    }

    private static RoomTemplateException rejected(String templateJson) {
        return assertThrows(RoomTemplateException.class,
                () -> RoomTemplateLibrary.fromJson("test.json", "{\"templates\": [" + templateJson + "]}"));
    }

    @Test
    void placeholderFileLoadsWithAllKinds() {
        RoomTemplateLibrary library = PlaceholderLibrary.load();
        for (RoomShape shape : RoomShape.values()) {
            assertTrue(library.templates(RoomKind.NORMAL, shape).size() >= 4, shape.name());
        }
        assertTrue(library.templates(RoomKind.PUZZLE).size() >= 2);
        assertFalse(library.templates(RoomKind.ENTRANCE).isEmpty());
        assertFalse(library.templates(RoomKind.FAIRY).isEmpty());
        assertFalse(library.templates(RoomKind.BLOOD).isEmpty());
        assertFalse(library.templates(RoomKind.MINIBOSS).isEmpty());
        assertFalse(library.templates(RoomKind.TRAP).isEmpty());
        assertTrue(library.all().stream().allMatch(RoomTemplate::placeholder));
        assertTrue(library.find("placeholder_puzzle_three_weirdos").isPresent());
    }

    @Test
    void validTemplateLoads() {
        RoomTemplateLibrary library = RoomTemplateLibrary.fromJson("test.json", "{\"templates\": [" + template("") + "]}");
        RoomTemplate loaded = library.find("t1").orElseThrow();
        assertEquals(RoomShape.ONE_BY_ONE, loaded.shape());
        assertEquals(1, loaded.starredSpawns().size());
        assertFalse(loaded.placeholder());
    }

    @Test
    void unknownShapeNamesTemplateAndField() {
        String message = rejected(template("ONE_BY_ONE=>HEXAGON")).getMessage();
        assertTrue(message.contains("'t1'") && message.contains("'shape'") && message.contains("HEXAGON"), message);
    }

    @Test
    void missingIdIsReported() {
        String message = rejected("{\"name\": \"x\"}").getMessage();
        assertTrue(message.contains("'id'") && message.contains("#0"), message);
    }

    @Test
    void negativeSecretCountIsRejected() {
        String message = rejected(template("\"secretCount\": 1=>\"secretCount\": -2")).getMessage();
        assertTrue(message.contains("'t1'") && message.contains("'secretCount'"), message);
    }

    @Test
    void fractionalSecretCountIsRejected() {
        String message = rejected(template("\"secretCount\": 1=>\"secretCount\": 1.5")).getMessage();
        assertTrue(message.contains("'secretCount'"), message);
    }

    @Test
    void multiCellSpecialRoomIsRejected() {
        String message = rejected(template("\"NORMAL\"=>\"PUZZLE\"").replace("ONE_BY_ONE", "ONE_BY_TWO")).getMessage();
        assertTrue(message.contains("'t1'") && message.contains("'shape'") && message.contains("PUZZLE"), message);
    }

    @Test
    void socketOutsideShapeIsRejected() {
        String message = rejected(template("\"col\": 0=>\"col\": 4")).getMessage();
        assertTrue(message.contains("'t1'") && message.contains("'doorSockets'"), message);
    }

    @Test
    void unknownSocketSideIsRejected() {
        String message = rejected(template("NORTH=>UP")).getMessage();
        assertTrue(message.contains("'side'") && message.contains("UP"), message);
    }

    @Test
    void spawnOutsideFootprintIsRejected() {
        String message = rejected(template("\"x\": 5=>\"x\": 99")).getMessage();
        assertTrue(message.contains("'t1'") && message.contains("'mobSpawns'"), message);
    }

    @Test
    void missingStarredFlagIsRejected() {
        String message = rejected(template(", \"starred\": true=>")).getMessage();
        assertTrue(message.contains("'starred'"), message);
    }

    @Test
    void unknownSecretTypeIsRejected() {
        String message = rejected(template("CHEST=>DIAMOND")).getMessage();
        assertTrue(message.contains("'type'") && message.contains("DIAMOND"), message);
    }

    @Test
    void moreAnchorsThanSecretCountIsRejected() {
        String message = rejected(template("\"secretCount\": 1=>\"secretCount\": 0")).getMessage();
        assertTrue(message.contains("'secretAnchors'"), message);
    }

    @Test
    void duplicateIdsAreRejected() {
        RoomTemplateException exception = assertThrows(RoomTemplateException.class,
                () -> RoomTemplateLibrary.fromJson("test.json", "{\"templates\": [" + template("") + "," + template("") + "]}"));
        assertTrue(exception.getMessage().contains("'t1'"), exception.getMessage());
    }

    @Test
    void invalidJsonAndMissingArrayAreRejected() {
        assertThrows(RoomTemplateException.class, () -> RoomTemplateLibrary.fromJson("bad.json", "{not json"));
        RoomTemplateException exception = assertThrows(RoomTemplateException.class, () -> RoomTemplateLibrary.fromJson("bad.json", "{}"));
        assertTrue(exception.getMessage().contains("'templates'"), exception.getMessage());
    }

    @Test
    void missingDirectoryIsRejected() {
        assertThrows(RoomTemplateException.class, () -> RoomTemplateLibrary.fromDirectory(java.nio.file.Path.of("does/not/exist")));
    }

    @Test
    void generatorRejectsLibraryWithoutRequiredKinds() {
        RoomTemplateLibrary library = RoomTemplateLibrary.fromJson("test.json", "{\"templates\": [" + template("") + "]}");
        assertThrows(IllegalStateException.class, () -> FloorLayoutGenerator.generate(
                dev.daweapon.dungeons.catacombs.CatacombsFloor.FLOOR_ONE,
                dev.daweapon.dungeons.catacombs.CatacombsMode.NORMAL, 1L, library));
    }

    @Test
    void shapeRotationKeepsCellCountAndNormalisesToOrigin() {
        for (RoomShape shape : RoomShape.values()) {
            for (Rotation rotation : Rotation.values()) {
                List<Cell> cells = shape.cells(rotation);
                assertEquals(shape.cellCount(), cells.stream().distinct().count());
                assertEquals(0, cells.stream().mapToInt(Cell::col).min().orElseThrow());
                assertEquals(0, cells.stream().mapToInt(Cell::row).min().orElseThrow());
            }
        }
        assertEquals(List.of(new Cell(0, 0), new Cell(0, 1)), RoomShape.ONE_BY_TWO.cells(Rotation.DEG_90));
    }
}
