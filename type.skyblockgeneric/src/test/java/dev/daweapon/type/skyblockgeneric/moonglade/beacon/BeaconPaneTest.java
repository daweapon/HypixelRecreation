package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconPaneTest {
    @Test
    void paneBouncesAcrossTheRow() {
        assertEquals(0, BeaconPane.bounce(0));
        assertEquals(8, BeaconPane.bounce(8));
        assertEquals(7, BeaconPane.bounce(9));
        assertEquals(0, BeaconPane.bounce(16));
    }

    @Test
    void higherSpeedMovesFaster() {
        BeaconPane slow = BeaconPane.start(0, 1, 1, 0);
        BeaconPane fast = BeaconPane.start(0, 5, 1, 0);
        assertEquals(1, slow.position(BeaconPane.stepMillis(1)));
        assertEquals(5, fast.position(5 * BeaconPane.stepMillis(5)));
    }

    @Test
    void pausedPaneStaysPutAndResumesFromThere() {
        BeaconPane pane = BeaconPane.start(0, 2, 1, 0).pausedAt(3 * BeaconPane.stepMillis(2));
        assertEquals(3, pane.position(100_000));
        BeaconPane resumed = pane.resumedAt(100_000);
        assertEquals(4, resumed.position(100_000 + BeaconPane.stepMillis(2)));
    }

    @Test
    void changingSpeedKeepsThePosition() {
        BeaconPane pane = BeaconPane.start(0, 1, 1, 0);
        long now = 4 * BeaconPane.stepMillis(1);
        assertEquals(4, pane.position(now));
        assertEquals(4, pane.withSpeed(3, now).position(now));
    }

    @Test
    void speedAndPitchAreClamped() {
        BeaconPane pane = BeaconPane.start(0, 3, 3, 0);
        assertEquals(BeaconPane.MAX_LEVEL, pane.withSpeed(99, 0).speed());
        assertEquals(BeaconPane.MIN_LEVEL, pane.withPitch(-4).pitch());
        assertEquals(BeaconPane.COLORS - 1, pane.withColor(-1).color());
    }

    @Test
    void solvedNeedsMatchingSettingsAndTheSamePosition() {
        BeaconPane target = BeaconPane.start(2, 3, 4, 0);
        BeaconTune tune = new BeaconTune(target, BeaconPane.start(2, 3, 4, 0));
        assertTrue(tune.solved(0));
        BeaconTune off = new BeaconTune(target, BeaconPane.start(2, 3, 4, 0).pausedAt(0));
        long later = 2 * BeaconPane.stepMillis(3);
        assertFalse(off.solved(later));
        assertTrue(off.solved(0));
        BeaconTune wrongColor = new BeaconTune(target, BeaconPane.start(1, 3, 4, 0));
        assertFalse(wrongColor.solved(0));
    }

    @Test
    void randomTuneNeverStartsSolved() {
        java.util.Random random = new java.util.Random(7);
        for (int i = 0; i < 200; i++) {
            BeaconTune tune = BeaconTune.random(random::nextInt, 0);
            assertFalse(tune.settingsMatch());
            assertTrue(tune.player().paused());
        }
    }

    @Test
    void cooldownBlocksUntilTenMinutesPass() {
        java.util.UUID id = java.util.UUID.randomUUID();
        BeaconCooldowns.mark(id, 1000);
        assertEquals(BeaconCooldowns.TUNE_COOLDOWN_MILLIS - 500, BeaconCooldowns.remainingMillis(id, 1500));
        assertEquals(0, BeaconCooldowns.remainingMillis(id, 1000 + BeaconCooldowns.TUNE_COOLDOWN_MILLIS));
    }
}
