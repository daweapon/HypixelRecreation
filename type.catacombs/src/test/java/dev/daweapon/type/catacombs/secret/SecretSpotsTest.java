package dev.daweapon.type.catacombs.secret;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.layout.Cell;
import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.FloorLayoutGenerator;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplateLibrary;
import dev.daweapon.dungeons.catacombs.secret.CatacombsSecretType;
import dev.daweapon.dungeons.catacombs.secret.SecretRewardType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretSpotsTest {
    private static RoomTemplateLibrary library;

    @BeforeAll
    static void load() {
        Path rooms = Path.of("..", "configuration", "skyblock", "dungeons", "rooms");
        Assumptions.assumeTrue(Files.isDirectory(rooms), "room templates not found next to the module");
        library = RoomTemplateLibrary.fromDirectory(rooms);
    }

    @Test
    void everyAnchorLandsInsideItsOwnRoomAtItsHeight() {
        for (long seed = 0; seed < 25; seed++) {
            FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, seed, library);
            List<SecretSpot> spots = SecretSpots.of(layout);
            int anchors = layout.rooms().stream().mapToInt(room -> room.template().secretAnchors().size()).sum();
            assertEquals(anchors, spots.size());
            Set<SecretLedger.Key> keys = new HashSet<>();
            for (SecretSpot spot : spots) {
                assertTrue(keys.add(spot.key()), "duplicate secret key " + spot.key());
                assertEquals(CellGeometry.FLOOR_Y + layout.room(spot.room()).template().secretAnchors().get(spot.anchor()).y(), spot.y());
                Cell cell = new Cell((spot.x() - CellGeometry.ORIGIN_X) / CellGeometry.CELL_PITCH,
                        (spot.z() - CellGeometry.ORIGIN_Z) / CellGeometry.CELL_PITCH);
                assertTrue(layout.room(spot.room()).occupies(cell), "seed " + seed + " spot " + spot);
            }
        }
    }

    @Test
    void ledgerTotalsMatchTheLayout() {
        FloorLayout layout = FloorLayoutGenerator.generate(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 42L, library);
        SecretLedger ledger = SecretLedger.of(layout);
        int total = layout.rooms().stream().mapToInt(room -> ledger.totalIn(room.index())).sum();
        assertEquals(layout.totalSecrets(), total);
    }

    @Test
    void rewardTablesFollowTheFloorAndSecretKind() {
        List<SecretRewardTable.Entry> chest = SecretRewardTable.entries(CatacombsFloor.FLOOR_ONE, CatacombsSecretType.CHEST);
        List<SecretRewardTable.Entry> ground = SecretRewardTable.entries(CatacombsFloor.FLOOR_ONE, CatacombsSecretType.GROUND_ITEM);
        assertTrue(chest.stream().anyMatch(SecretRewardTable.Entry::blessing));
        assertFalse(ground.stream().anyMatch(SecretRewardTable.Entry::blessing));
        assertTrue(ground.stream().allMatch(entry -> entry.item() != null));
        assertFalse(chest.stream().anyMatch(entry -> entry.type() == SecretRewardType.DUNGEON_CHEST_KEY));
        assertTrue(SecretRewardTable.entries(CatacombsFloor.FLOOR_FOUR, CatacombsSecretType.CHEST).stream()
                .anyMatch(entry -> entry.type() == SecretRewardType.DUNGEON_CHEST_KEY));
        assertTrue(SecretRewardTable.entries(CatacombsFloor.FLOOR_ONE, CatacombsSecretType.WITHER_ESSENCE).isEmpty());
    }

    @Test
    void rollingAlwaysPicksFromTheTable() {
        Random random = new Random(7);
        List<SecretRewardTable.Entry> bat = SecretRewardTable.entries(CatacombsFloor.FLOOR_ONE, CatacombsSecretType.BAT);
        for (int roll = 0; roll < 500; roll++) {
            SecretRewardTable.Entry entry = SecretRewardTable.roll(null, CatacombsFloor.FLOOR_ONE, CatacombsSecretType.BAT, random);
            assertNotNull(entry);
            assertTrue(bat.contains(entry) || entry.type() == SecretRewardType.SECRET_DYE);
        }
        assertNull(SecretRewardTable.roll(null, CatacombsFloor.FLOOR_ONE, CatacombsSecretType.WITHER_ESSENCE, random));
    }
}
