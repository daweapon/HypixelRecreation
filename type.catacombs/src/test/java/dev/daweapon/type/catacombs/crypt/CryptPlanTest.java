package dev.daweapon.type.catacombs.crypt;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.CatacombsRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.PlacedRoom;
import dev.daweapon.dungeons.catacombs.layout.RoomKind;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunConfig;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.type.catacombs.secret.SecretSpot;
import dev.daweapon.type.catacombs.secret.SecretSpots;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptPlanTest {
    private static RoomTemplateLibrary library;

    @BeforeAll
    static void load() {
        Path rooms = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
        Assumptions.assumeTrue(Files.isDirectory(rooms), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(rooms);
    }

    @Test
    void cryptsSitInStandardRoomsClearOfSecretsAndAlwaysReachTheBonusCap() {
        for (long seed = 0; seed < 25; seed++) {
            FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, seed, library);
            List<CryptPlan.Crypt> crypts = CryptPlan.of(layout);
            assertTrue(crypts.size() >= CryptPlan.MINIMUM, "seed " + seed + " has only " + crypts.size() + " crypts");
            List<SecretSpot> secrets = SecretSpots.of(layout);
            for (CryptPlan.Crypt crypt : crypts) {
                PlacedRoom room = layout.room(crypt.room());
                assertTrue(room.kind() == RoomKind.NORMAL || room.kind() == RoomKind.RARE);
                for (int dx = 0; dx < CryptPlan.TOMB_LENGTH; dx++) {
                    int x = crypt.x() + dx;
                    assertTrue(room.occupies(new Cell((x - CellGeometry.ORIGIN_X) / CellGeometry.CELL_PITCH, (crypt.z() - CellGeometry.ORIGIN_Z) / CellGeometry.CELL_PITCH)));
                    assertFalse(secrets.stream().anyMatch(spot -> spot.at(x, crypt.y(), crypt.z())));
                }
            }
            assertEquals(crypts, CryptPlan.of(layout));
        }
    }

    @Test
    void cryptBonusCapsAtFiveWhileTheCounterKeepsCounting() {
        var definition = CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL);
        CatacombsRunState state = CatacombsRunState.start(new CatacombsRunConfig(definition, Map.of(UUID.randomUUID(), DungeonClassType.MAGE), 10, 20, true));
        state.recordCrypt();
        state.recordCrypt();
        int twoCrypts = state.score().bonus();
        for (int crypt = 0; crypt < 5; crypt++) state.recordCrypt();
        assertEquals(2, twoCrypts);
        assertEquals(7, state.crypts());
        assertEquals(5, state.score().bonus());
    }
}
