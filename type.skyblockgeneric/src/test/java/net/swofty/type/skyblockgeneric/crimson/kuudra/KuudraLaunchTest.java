package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraLaunchTest {
    private static final Path SHIPPED = Path.of("../configuration/skyblock/kuudra/launch.yml");

    @TempDir
    Path directory;

    @AfterEach
    void restore() {
        KuudraAvailability.use(new KuudraLaunch(KuudraLaunch.FILE, System::currentTimeMillis));
    }

    @Test
    void theShippedSwitchIsOff() throws Exception {
        assertTrue(Files.isRegularFile(SHIPPED));
        assertFalse(KuudraLaunch.read(SHIPPED));
    }

    @Test
    void offKeepsKuudraStaffOnlyAndItsXpTasksOutOfTheGame() throws IOException {
        Path file = write("public: false\n", 1);
        KuudraAvailability.use(new KuudraLaunch(file, () -> 0));

        assertFalse(KuudraAvailability.isFightAvailable());
        assertEquals(SkyBlockXPSource.Availability.NOT_IN_GAME, SkyBlockXPSource.DEFEAT_KUUDRA.getAvailability());
        assertEquals(SkyBlockXPSource.Availability.NOT_IN_GAME, SkyBlockXPSource.BOSS_COLLECTIONS.getAvailability());
        assertFalse(SkyBlockXPSource.DEFEAT_KUUDRA.isObtainable());
        assertEquals(KuudraAvailability.UNAVAILABLE_LORE, KuudraAvailability.lore().getLast());
    }

    @Test
    void onOpensKuudraAndMakesBothXpTasksLive() throws IOException {
        Path file = write("public: true\n", 1);
        KuudraAvailability.use(new KuudraLaunch(file, () -> 0));

        assertTrue(KuudraAvailability.isFightAvailable());
        assertEquals(SkyBlockXPSource.Availability.LIVE, SkyBlockXPSource.DEFEAT_KUUDRA.getAvailability());
        assertEquals(SkyBlockXPSource.Availability.LIVE, SkyBlockXPSource.BOSS_COLLECTIONS.getAvailability());
        assertTrue(SkyBlockXPSource.BOSS_COLLECTIONS.isObtainable());
        assertTrue(KuudraAvailability.lore().isEmpty());
    }

    @Test
    void otherXpTasksIgnoreTheSwitch() throws IOException {
        KuudraAvailability.use(new KuudraLaunch(write("public: true\n", 1), () -> 0));
        assertEquals(SkyBlockXPSource.Availability.NOT_IN_GAME, SkyBlockXPSource.DEFEAT_ARACHNE.getAvailability());
        assertEquals(SkyBlockXPSource.Availability.LIVE, SkyBlockXPSource.SLAYER_LEVEL_UP.getAvailability());
    }

    @Test
    void aConfigEditIsPickedUpWithoutARestart() throws IOException {
        AtomicLong now = new AtomicLong();
        Path file = write("public: false\n", 1);
        KuudraLaunch launch = new KuudraLaunch(file, now::get);
        assertFalse(launch.isPublic());

        write("public: true\n", 2);
        assertFalse(launch.isPublic(), "re-read no more often than every few seconds");
        now.addAndGet(KuudraLaunch.RECHECK_MILLIS);
        assertTrue(launch.isPublic());

        write("public: false\n", 3);
        now.addAndGet(KuudraLaunch.RECHECK_MILLIS);
        assertFalse(launch.isPublic());
    }

    @Test
    void aMissingOrBrokenFileMeansNotLaunched() throws IOException {
        assertFalse(new KuudraLaunch(directory.resolve("absent.yml"), () -> 0).isPublic());
        assertFalse(new KuudraLaunch(write("public: [oops\n", 1), () -> 0).isPublic());
        assertFalse(new KuudraLaunch(write("public: yes please\n", 1), () -> 0).isPublic());
    }

    private Path write(String content, long secondsStamp) throws IOException {
        Path file = directory.resolve("launch.yml");
        Files.writeString(file, content);
        Files.setLastModifiedTime(file, FileTime.fromMillis(secondsStamp * 1000));
        return file;
    }
}
