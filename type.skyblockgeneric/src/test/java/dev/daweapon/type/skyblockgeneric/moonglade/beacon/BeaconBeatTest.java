package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconBeatTest {
    @Test
    void randomBeatUsesTheSuppliedPicks() {
        int[] picks = {2, 0, 1};
        int[] index = {0};
        BeaconBeat beat = BeaconBeat.random(bound -> picks[index[0]++] % bound);
        assertEquals(new BeaconBeat(BeaconBeat.Color.YELLOW, BeaconBeat.Speed.SLOW, BeaconBeat.Pitch.MID), beat);
    }

    @Test
    void sequenceHasOneBeatPerPanel() {
        List<BeaconBeat> beats = BeaconBeat.sequence(2, bound -> 0);
        assertEquals(2, beats.size());
        assertEquals(BeaconBeat.Color.RED, beats.getFirst().color());
    }

    @Test
    void matchesOnlyWhenColorSpeedAndPitchAllAgree() {
        BeaconBeat beat = new BeaconBeat(BeaconBeat.Color.LIME, BeaconBeat.Speed.FAST, BeaconBeat.Pitch.HIGH);
        assertTrue(beat.matches(BeaconBeat.Color.LIME, BeaconBeat.Speed.FAST, BeaconBeat.Pitch.HIGH));
        assertFalse(beat.matches(BeaconBeat.Color.RED, BeaconBeat.Speed.FAST, BeaconBeat.Pitch.HIGH));
        assertFalse(beat.matches(BeaconBeat.Color.LIME, BeaconBeat.Speed.SLOW, BeaconBeat.Pitch.HIGH));
        assertFalse(beat.matches(BeaconBeat.Color.LIME, BeaconBeat.Speed.FAST, BeaconBeat.Pitch.LOW));
    }

    @Test
    void flashesAtTheBeatSpeed() {
        BeaconBeat slow = new BeaconBeat(BeaconBeat.Color.RED, BeaconBeat.Speed.SLOW, BeaconBeat.Pitch.LOW);
        assertTrue(slow.lit(0));
        assertTrue(slow.lit(499));
        assertFalse(slow.lit(500));
        assertTrue(slow.lit(1000));
        BeaconBeat fast = new BeaconBeat(BeaconBeat.Color.RED, BeaconBeat.Speed.FAST, BeaconBeat.Pitch.LOW);
        assertFalse(fast.lit(100));
        assertTrue(fast.lit(200));
    }
}
