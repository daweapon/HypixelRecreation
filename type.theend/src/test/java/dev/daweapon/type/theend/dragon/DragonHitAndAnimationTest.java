package dev.daweapon.type.theend.dragon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DragonHitAndAnimationTest {

    @Test
    void clientBodyPartsAreTheEightIdsAfterTheDragon() {
        assertFalse(DragonMeleeRedirect.isPart(100, 100));
        for (int id = 101; id <= 108; id++) assertTrue(DragonMeleeRedirect.isPart(id, 100));
        assertFalse(DragonMeleeRedirect.isPart(109, 100));
        assertFalse(DragonMeleeRedirect.isPart(99, 100));
    }

    @Test
    void glassColumnRisesFromTheAltarToTheEgg() {
        assertEquals(0, GlassRise.heightAt(0));
        assertEquals(GlassRise.EGG_Y - GlassRise.BASE_Y, GlassRise.heightAt(GlassRise.RISE_TICKS));
        assertEquals(GlassRise.EGG_Y - GlassRise.BASE_Y, GlassRise.heightAt(GlassRise.RISE_TICKS * 2));
        assertTrue(GlassRise.heightAt(50) > 0 && GlassRise.heightAt(50) < GlassRise.heightAt(80));
    }

    @Test
    void gateMessagesFollowCumulativeDamage() {
        assertEquals(0, DragonGate.messagesFor(0, 299_999).length);
        assertEquals("The Dragon's Gate is weakening!", DragonGate.messagesFor(0, 300_000)[0]);
        assertEquals(2, DragonGate.messagesFor(0, 600_000).length);
        assertEquals(1, DragonGate.messagesFor(1, 600_000).length);
        assertEquals(4, DragonGate.messagesFor(0, 5_000_000).length);
        assertEquals(0, DragonGate.messagesFor(4, 5_000_000).length);
    }
}
