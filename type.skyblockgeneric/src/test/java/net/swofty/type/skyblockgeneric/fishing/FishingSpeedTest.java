package net.swofty.type.skyblockgeneric.fishing;

import net.swofty.commons.ServerType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FishingSpeedTest {

    @Test
    void crimsonIsleCapIs350() {
        assertEquals(350D, FishingSpeed.cap(ServerType.SKYBLOCK_CRIMSON_ISLE));
        assertEquals(200D, FishingSpeed.cap(ServerType.SKYBLOCK_BACKWATER_BAYOU));
        assertEquals(300D, FishingSpeed.cap(ServerType.SKYBLOCK_HUB));
    }

    @Test
    void noSpeedWaitsTheFullBaseAndTheCapBitesAtOnce() {
        Random random = new Random(3);
        for (int i = 0; i < 1000; i++) {
            long ticks = FishingSpeed.waitTicks(0, 350, random);
            assertTrue(ticks >= 200 && ticks <= 400, String.valueOf(ticks));
            assertEquals(1L, FishingSpeed.waitTicks(350, 350, random));
        }
    }

    @Test
    void halfTheCapHalvesTheWait() {
        long ticks = FishingSpeed.waitTicks(175, 350, new Random(1));
        assertTrue(ticks >= 100 && ticks <= 200, String.valueOf(ticks));
    }
}
