package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganDonorTest {
    @Test
    void pitchEncodesDistanceAndRoundTrips() {
        for (double distance : new double[]{0, 1.5, 4, 10, 19.5}) {
            assertEquals(distance, OrganDonor.distance(OrganDonor.pitch(distance)), 0.001);
        }
        assertEquals(2, OrganDonor.pitch(0), 0.05);
        assertEquals(1, OrganDonor.pitch(20), 0.05);
    }

    @Test
    void dingsSlowDownAsTheCorpseGetsFarther() {
        assertTrue(OrganDonor.gapMillis(2) < OrganDonor.gapMillis(8));
        assertTrue(OrganDonor.gapMillis(8) < OrganDonor.gapMillis(18));
    }
}
