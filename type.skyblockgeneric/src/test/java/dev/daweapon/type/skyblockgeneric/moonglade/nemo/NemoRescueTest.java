package dev.daweapon.type.skyblockgeneric.moonglade.nemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NemoRescueTest {
    @Test
    void firstDrowningHasFourLinesAndLaterOnesAdvance() {
        assertEquals(4, NemoRescue.dialogue(1).size());
        assertEquals(2, NemoRescue.dialogue(2).size());
        assertEquals(3, NemoRescue.dialogue(3).size());
        assertNotEquals(NemoRescue.dialogue(3), NemoRescue.dialogue(4));
    }

    @Test
    void dialogueStaysOnTheLastVariant() {
        assertEquals(NemoRescue.dialogue(4), NemoRescue.dialogue(40));
    }
}
