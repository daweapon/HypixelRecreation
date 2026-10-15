package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraWavesAndLivesTest {
    private static final KuudraConfig CONFIG = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra"));
    private static final KuudraFightSpec.Waves WAVES = CONFIG.fight().waves();

    private static Map<String, Integer> plan(int wave, int party) {
        return KuudraWavePlan.plan(wave, CONFIG.waves(), party, WAVES).stream()
                .collect(Collectors.toMap(KuudraWavePlan.Batch::mobId, KuudraWavePlan.Batch::count));
    }

    @Test
    void theFirstWavesFollowTheWikiListAndGrowWithTheParty() {
        assertEquals(Map.of("KUUDRA_FOLLOWER", 2, "INFERNO_MAGMA_CUBE", 2, "KUUDRA_LANDMINE", 2), plan(1, 1));
        assertEquals(Map.of("KUUDRA_FOLLOWER", 5, "INFERNO_MAGMA_CUBE", 5, "KUUDRA_LANDMINE", 5), plan(1, 4));
        assertEquals(1, plan(5, 4).get("DROPSHIP"));
        assertEquals(5, plan(14, 2).get("MAGMA_FOLLOWER"));
        assertEquals(Map.of("DROPSHIP", 8), plan(20, 4));
    }

    @Test
    void laterWavesRepeatBiggerWithDropshipsAndChaosmites() {
        assertEquals(Map.of("KUUDRA_FOLLOWER", 3, "INFERNO_MAGMA_CUBE", 3, "KUUDRA_LANDMINE", 3), plan(21, 1));
        assertEquals(2, plan(25, 1).get("DROPSHIP"));
        assertFalse(plan(34, 1).containsKey("CHAOSMITE"));
        assertEquals(1, plan(35, 1).get("CHAOSMITE"));
        assertEquals(2, plan(45, 1).get("CHAOSMITE"));
        assertTrue(KuudraWavePlan.plan(0, CONFIG.waves(), 1, WAVES).isEmpty());
    }

    @Test
    void spawningStopsAtTheCapOnLivingMobs() {
        assertEquals(5, KuudraWavePlan.allowed(0, 30, 5));
        assertEquals(2, KuudraWavePlan.allowed(28, 30, 5));
        assertEquals(0, KuudraWavePlan.allowed(31, 30, 5));
    }

    @Test
    void everyWaveMobHasStatsForEveryTier() {
        for (int wave = 1; wave <= 40; wave++) {
            for (KuudraWavePlan.Batch batch : KuudraWavePlan.plan(wave, CONFIG.waves(), 4, WAVES)) {
                assertTrue(CONFIG.mob(batch.mobId()) != null, batch.mobId());
                assertEquals(5, CONFIG.mob(batch.mobId()).stats().size(), batch.mobId());
            }
        }
    }

    @Test
    void autoReviveGhostsRespawnAndWipes() {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        KuudraLives lives = new KuudraLives(10, 3);
        assertEquals(KuudraLives.Death.AUTO_REVIVED, lives.die(alice, 0, false, true, 0));
        assertTrue(lives.isInvulnerable(alice, 59));
        assertFalse(lives.isInvulnerable(alice, 60));
        assertFalse(lives.isGhost(alice));

        assertEquals(KuudraLives.Death.GHOST, lives.die(alice, 100, false, false, 0.25));
        assertEquals(150, lives.respawnTicks(0.25));
        assertEquals(8, lives.secondsToRespawn(alice, 100));
        assertFalse(lives.wiped(List.of(alice, bob)));
        assertEquals(KuudraLives.Death.FINAL, lives.die(bob, 120, true, true, 0));
        assertTrue(lives.wiped(List.of(alice, bob)));
        assertTrue(lives.tick(249).isEmpty());
        assertEquals(List.of(alice), lives.tick(250));
        assertTrue(lives.isInvulnerable(alice, 250));
        assertTrue(lives.isFinal(bob));
        assertEquals(-1, lives.secondsToRespawn(bob, 300));
        assertTrue(lives.revive(bob, 300));
        assertFalse(lives.revive(bob, 301));
    }
}
