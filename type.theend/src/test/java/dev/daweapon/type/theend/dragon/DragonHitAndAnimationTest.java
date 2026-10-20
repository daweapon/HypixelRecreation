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

    @Test
    void downMessageListsTopThreeThenTheViewersOwnDamage() {
        var top = java.util.List.of(
                new DragonDownMessage.Damager(new DragonDownMessage.Name("<b>[MVP+] A", "[MVP+] A"), 11_595_396),
                new DragonDownMessage.Damager(new DragonDownMessage.Name("<a>B", "B"), 845_550),
                new DragonDownMessage.Damager(new DragonDownMessage.Name("<7>C", "C"), 92_737));
        var lines = DragonDownMessage.lines("Wise Dragon", new DragonDownMessage.Name("<a>B", "B"), top, 4, 50_440, 5);
        assertTrue(lines.getFirst().contains("▬") && lines.getLast().contains("▬"));
        assertTrue(lines.stream().anyMatch(line -> line.contains("WISE DRAGON DOWN!")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("dealt the final blow.")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("1st Damager") && line.contains("11,595,396")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("3rd Damager") && line.contains("92,737")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Your Damage: <a>50,440") && line.contains("Position #4")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("+5 Dragon Essence")));
    }

    @Test
    void spectatorsGetNoPersonalLines() {
        var lines = DragonDownMessage.lines("Old Dragon", null, java.util.List.of(), 0, 0, 5);
        assertTrue(lines.stream().noneMatch(line -> line.contains("Your Damage") || line.contains("Essence")));
    }
}
