package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonClassProgressTest {
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
    void storedClassFallsBackToTheDefault() {
        assertEquals(DungeonClassType.MAGE, DungeonClassProgress.parse("MAGE"));
        assertEquals(DungeonClassProgress.DEFAULT_CLASS, DungeonClassProgress.parse(""));
        assertEquals(DungeonClassProgress.DEFAULT_CLASS, DungeonClassProgress.parse(null));
        assertEquals(DungeonClassProgress.DEFAULT_CLASS, DungeonClassProgress.parse("NECROMANCER"));
    }

    @Test
    void runAndLevelUpLines() {
        assertEquals("<3>+<b>80 Mage Experience", DungeonClassProgress.runXpLine(DungeonClassType.MAGE, 80, false));
        assertEquals("<3>+<b>1,250 Tank Experience <8>(Team Bonus)", DungeonClassProgress.runXpLine(DungeonClassType.TANK, 1_250, true));
        List<String> lines = DungeonClassProgress.levelUpLines(DungeonClassType.HEALER, 4, 6);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Healer <8>4➜<3>6")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("+<b>8 SkyBlock XP")));
    }

    @Test
    void classLevelUpXpFollowsTheCatacombsLaunchWithoutChangingTheMaximum() throws IOException {
        int maximum = SkyBlockXPSource.TOTAL_MAX_XP;
        launch("public: false\n");
        assertEquals(SkyBlockXPSource.Availability.NOT_IN_GAME, SkyBlockXPSource.CLASS_LEVEL_UP.getAvailability());
        launch("public: true\n");
        assertEquals(SkyBlockXPSource.Availability.LIVE, SkyBlockXPSource.CLASS_LEVEL_UP.getAvailability());
        assertEquals(1_000, SkyBlockXPSource.CLASS_LEVEL_UP.getMaxXP());
        assertEquals(maximum, SkyBlockXPSource.TOTAL_MAX_XP);
    }
}
