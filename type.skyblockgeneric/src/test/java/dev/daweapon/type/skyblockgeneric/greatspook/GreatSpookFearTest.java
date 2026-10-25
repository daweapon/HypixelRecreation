package dev.daweapon.type.skyblockgeneric.greatspook;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreatSpookFearTest {

    @Test
    void swordGainsOnePerFear() {
        assertEquals(0, GreatSpookFear.swordBonus(0));
        assertEquals(96, GreatSpookFear.swordBonus(96));
        assertEquals(0, GreatSpookFear.swordBonus(-5));
    }

    @Test
    void monstersAtOrBelowFearRunAway() {
        assertTrue(GreatSpookFear.frightens(29, 29));
        assertTrue(GreatSpookFear.frightens(10, 29));
        assertFalse(GreatSpookFear.frightens(30, 29));
        assertFalse(GreatSpookFear.frightens(1, 0));
        assertFalse(GreatSpookFear.frightens(null, 100));
    }

    @Test
    void fullSetNeedsAllFourPieces() {
        assertTrue(GreatSpookFear.wearsFullSet(List.of("GREAT_SPOOK_HELMET", "GREAT_SPOOK_CHESTPLATE",
                "GREAT_SPOOK_LEGGINGS", "GREAT_SPOOK_BOOTS")));
        assertFalse(GreatSpookFear.wearsFullSet(List.of("GREAT_SPOOK_HELMET", "GREAT_SPOOK_CHESTPLATE",
                "GREAT_SPOOK_LEGGINGS", "")));
    }

    @Test
    void cooldownCountsDownFromTheReadyTime() {
        UUID id = UUID.randomUUID();
        Map<UUID, Long> ready = Map.of(id, 10_000L);
        assertEquals(4_000, GreatSpookStaff.remainingMillis(ready, id, 6_000));
        assertEquals(0, GreatSpookStaff.remainingMillis(ready, id, 11_000));
        assertEquals(0, GreatSpookStaff.remainingMillis(ready, UUID.randomUUID(), 0));
    }

    @Test
    void everySpookEndsWithTheReassurance() {
        for (int seed = 0; seed < 50; seed++) {
            SpookerMessages.Spook spook = SpookerMessages.random(new Random(seed));
            assertTrue(spook.lines().getLast().markup().startsWith("<a>"), spook.name());
        }
    }

    @Test
    void theRevealNamesTheSpooker() {
        for (int seed = 0; seed < 20; seed++) {
            assertTrue(SpookerMessages.reveal(new Random(seed), "Bob").contains("Bob"));
        }
    }

    @Test
    void theEventDefaultsToOffWithNoWindow() {
        assertFalse(GreatSpookEvent.isActive());
    }
}
