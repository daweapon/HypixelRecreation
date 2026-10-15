package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsAvailabilityTest {
    private static final Path SHIPPED = Path.of("../configuration/skyblock/dungeons/launch.yml");

    @TempDir
    Path directory;

    @AfterEach
    void restore() {
        CatacombsAvailability.use(new CatacombsLaunch(CatacombsLaunch.FILE, System::currentTimeMillis));
    }

    private void launch(String content) throws IOException {
        Path file = directory.resolve("launch.yml");
        Files.writeString(file, content);
        Files.setLastModifiedTime(file, FileTime.fromMillis(1000));
        CatacombsAvailability.use(new CatacombsLaunch(file, () -> 0));
    }

    @Test
    void theShippedSwitchIsOff() throws Exception {
        assertTrue(Files.isRegularFile(SHIPPED));
        assertFalse(CatacombsLaunch.read(SHIPPED));
    }

    @Test
    void whileStaffOnlyOnlyStaffQueueThePlayableFloors() throws IOException {
        launch("public: false\n");
        assertFalse(CatacombsAvailability.isPublic());
        assertTrue(CatacombsAvailability.canQueue(true, CatacombsFloor.ENTRANCE));
        assertTrue(CatacombsAvailability.canQueue(true, CatacombsFloor.FLOOR_ONE));
        assertFalse(CatacombsAvailability.canQueue(false, CatacombsFloor.ENTRANCE));
        assertFalse(CatacombsAvailability.canQueue(false, CatacombsFloor.FLOOR_ONE));
    }

    @Test
    void publicOpensThePlayableFloorsToEveryone() throws IOException {
        launch("public: true\n");
        assertTrue(CatacombsAvailability.canQueue(false, CatacombsFloor.ENTRANCE));
        assertTrue(CatacombsAvailability.canQueue(false, CatacombsFloor.FLOOR_ONE));
    }

    @Test
    void laterFloorsStayLockedForEveryone() throws IOException {
        launch("public: true\n");
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            if (floor == CatacombsFloor.ENTRANCE || floor == CatacombsFloor.FLOOR_ONE) continue;
            assertFalse(CatacombsAvailability.isPlayable(floor), floor.name());
            assertFalse(CatacombsAvailability.canQueue(true, floor), floor.name());
        }
    }

    @Test
    void aMissingFileMeansStaffOnly() {
        CatacombsAvailability.use(new CatacombsLaunch(directory.resolve("absent.yml"), () -> 0));
        assertFalse(CatacombsAvailability.isPublic());
    }

    @Test
    void floorIdsMatchTheOrchestratorModes() {
        assertEquals("ENTRANCE", CatacombsFloorIds.of(CatacombsFloor.ENTRANCE));
        assertEquals("F1", CatacombsFloorIds.of(CatacombsFloor.FLOOR_ONE));
        assertEquals("F7", CatacombsFloorIds.of(CatacombsFloor.FLOOR_SEVEN));
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            assertEquals(floor, CatacombsFloorIds.parse(CatacombsFloorIds.of(floor)).orElseThrow());
        }
        assertTrue(CatacombsFloorIds.parse("MASTER_F1").isEmpty());
    }

    @Test
    void requirementsAreCombatForEntranceAndCatacombsLevelsAfter() {
        assertFalse(CatacombsRequirements.meets(14, 0, CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL));
        assertTrue(CatacombsRequirements.meets(15, 0, CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL));
        assertFalse(CatacombsRequirements.meets(60, 0, CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertTrue(CatacombsRequirements.meets(0, 1, CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertFalse(CatacombsRequirements.meets(60, 2, CatacombsFloor.FLOOR_TWO, CatacombsMode.NORMAL));
        assertTrue(CatacombsRequirements.meets(0, 3, CatacombsFloor.FLOOR_TWO, CatacombsMode.NORMAL));
        assertFalse(CatacombsRequirements.meets(60, 50, CatacombsFloor.FLOOR_ONE, CatacombsMode.MASTER));
    }
}
