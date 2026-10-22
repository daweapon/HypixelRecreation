package dev.daweapon.type.skyblockgeneric.entity.mob.island.dummy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingDummySetTest {
    @Test
    void roundTripKeepsPositionAndForm() {
        TrainingDummySet set = new TrainingDummySet();
        set.add(new PlacedTrainingDummy(10.5, 70, -3.5, 90f, TrainingDummyForm.WITCH));
        TrainingDummySet loaded = TrainingDummySet.parse(set.serialize());
        assertEquals(set.all(), loaded.all());
    }

    @Test
    void atMostFourDummies() {
        TrainingDummySet set = new TrainingDummySet();
        for (int i = 0; i < TrainingDummySet.MAX_DUMMIES; i++) {
            set.add(new PlacedTrainingDummy(i * 10, 70, 0, 0f, TrainingDummyForm.ZOMBIE));
        }
        assertTrue(set.isFull());
    }

    @Test
    void dummiesNeedFiveBlocksOfSpace() {
        TrainingDummySet set = new TrainingDummySet();
        set.add(new PlacedTrainingDummy(0, 70, 0, 0f, TrainingDummyForm.ZOMBIE));
        assertTrue(set.tooClose(4.9, 70, 0));
        assertFalse(set.tooClose(5, 70, 0));
    }

    @Test
    void changingFormReplacesTheEntry() {
        TrainingDummySet set = new TrainingDummySet();
        PlacedTrainingDummy dummy = new PlacedTrainingDummy(0, 70, 0, 0f, TrainingDummyForm.ZOMBIE);
        set.add(dummy);
        set.replace(dummy, dummy.withForm(TrainingDummyForm.BLAZE));
        assertEquals(TrainingDummyForm.BLAZE, set.all().getFirst().form());
        assertEquals(1, set.all().size());
    }

    @Test
    void sixteenFormsExist() {
        assertEquals(16, TrainingDummyForm.values().length);
    }
}
