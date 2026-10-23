package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HologramSettingsTest {
    @Test
    void roundTripsLinesParticleAndModel() {
        HologramSettings settings = HologramSettings.initial().withLine(0, "&aHello, SkyBlock").withLine(2, "&lWorld;with,odd chars").withParticle('c').withModelVisible(false);
        HologramSettings decoded = HologramSettings.decode(settings.encode());
        assertEquals("&aHello, SkyBlock", decoded.line(0));
        assertEquals("", decoded.line(1));
        assertEquals("&lWorld;with,odd chars", decoded.line(2));
        assertEquals('c', decoded.particle());
        assertEquals(false, decoded.modelVisible());
    }

    @Test
    void encodedDataNeverContainsEntrySeparators() {
        String encoded = HologramSettings.initial().withLine(0, "a;b,c\nd").encode();
        assertTrue(!encoded.contains(";") && !encoded.contains(","));
    }

    @Test
    void modelCannotBeHiddenWithoutText() {
        HologramSettings hidden = HologramSettings.initial().withModelVisible(false);
        assertTrue(hidden.hasText() || hidden.withLine(0, "x").withModelVisible(false).withLine(0, null).modelVisible());
    }

    @Test
    void trailingBlankLinesAreDroppedAndLinesCapped() {
        HologramSettings settings = HologramSettings.initial().withLine(0, "a").withLine(1, "b").withLine(2, "c").withLine(1, null).withLine(2, null);
        assertEquals(1, settings.lines().size());
    }

    @Test
    void particleCyclesThroughSixteenColoursThenNone() {
        HologramSettings settings = HologramSettings.initial().withParticle(null);
        for (int i = 0; i < HologramSettings.PARTICLE_COLOURS.length(); i++) settings = settings.nextParticle();
        assertEquals('f', settings.particle());
        assertNull(settings.nextParticle().particle());
    }

    @Test
    void missingDataIsTheDefaultHologram() {
        assertEquals(HologramSettings.initial(), HologramSettings.decode(null));
    }
}
