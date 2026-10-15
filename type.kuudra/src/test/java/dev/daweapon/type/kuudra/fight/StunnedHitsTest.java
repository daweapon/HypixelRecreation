package dev.daweapon.type.kuudra.fight;

import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraTier;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.data.KuudraFightSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StunnedHitsTest {
    private static final KuudraFightSpec.Stun STUN = KuudraConfig.load(Path.of("../configuration/skyblock/kuudra")).fight().stun();
    private static final UUID ARCHER = UUID.randomUUID();
    private static final UUID SWORDSMAN = UUID.randomUUID();

    private static StunnedHits hits() {
        return new StunnedHits(STUN.stunnedHitCooldownTicks(), STUN.stunnedArrowHitsPerTick());
    }

    @Test
    void meleeSpamCountsOncePerCooldown() {
        StunnedHits hits = hits();
        assertTrue(hits.melee(SWORDSMAN, 100));
        assertFalse(hits.melee(SWORDSMAN, 100));
        assertFalse(hits.melee(SWORDSMAN, 100 + STUN.stunnedHitCooldownTicks() - 1));
        assertTrue(hits.melee(SWORDSMAN, 100 + STUN.stunnedHitCooldownTicks()));
    }

    @Test
    void everyArrowOfAFullHydraVolleyCounts() {
        StunnedHits hits = hits();
        int volley = 3 + 2;
        for (int arrow = 0; arrow < volley; arrow++) assertTrue(hits.arrow(ARCHER, 200), "arrow " + arrow);
        assertEquals(volley, STUN.stunnedArrowHitsPerTick());
        assertFalse(hits.arrow(ARCHER, 200));
        assertTrue(hits.arrow(ARCHER, 201));
    }

    @Test
    void arrowsDoNotWaitOnTheMeleeCooldown() {
        StunnedHits hits = hits();
        assertTrue(hits.melee(ARCHER, 300));
        assertTrue(hits.arrow(ARCHER, 300));
        assertTrue(hits.arrow(ARCHER, 301));
        assertTrue(hits.arrow(SWORDSMAN, 301));
    }

    @Test
    void anArrowScoresItsShareOfAMeleeHit() {
        assertEquals(0.25, STUN.stunnedArrowHitFactor(), 1e-9);
        assertEquals(1500, KuudraStunCycle.points(STUN, KuudraTier.INFERNAL, false), 1e-9);
        assertEquals(375, KuudraStunCycle.points(STUN, KuudraTier.INFERNAL, true), 1e-9);
        assertEquals(625, KuudraStunCycle.points(STUN, KuudraTier.BURNING, true), 1e-9);
    }
}
