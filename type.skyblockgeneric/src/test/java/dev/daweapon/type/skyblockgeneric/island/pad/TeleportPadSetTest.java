package dev.daweapon.type.skyblockgeneric.island.pad;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeleportPadSetTest {
    @Test
    void fiftySixIconsAreAvailable() {
        assertEquals(56, TeleportPadIcon.all().size());
        assertEquals(56, new HashSet<>(TeleportPadIcon.all().stream().map(TeleportPadIcon::label).toList()).size());
        assertEquals("<c>Red", TeleportPadIcon.get(0).label());
        assertEquals("FortyThree", TeleportPadIcon.get(55).name());
    }

    @Test
    void numberWordsRead() {
        assertEquals("Thirteen", TeleportPadIcon.numberWord(13));
        assertEquals("TwentyOne", TeleportPadIcon.numberWord(21));
        assertEquals("Thirty", TeleportPadIcon.numberWord(30));
        assertEquals("FortyThree", TeleportPadIcon.numberWord(43));
    }

    @Test
    void padsRoundTripWithSettings() {
        TeleportPadSet set = new TeleportPadSet();
        TeleportPad first = set.add(1, 70, 2);
        TeleportPad second = set.add(-5, 71, 9);
        first.destination(second.key());
        first.direction(TeleportPadDirection.SOUTH_WEST);
        first.name("Mine, entrance; <b>");
        second.icon(33);

        TeleportPadSet loaded = TeleportPadSet.parse(set.serialize());
        TeleportPad loadedFirst = loaded.at(1, 70, 2);
        assertNotNull(loadedFirst);
        assertEquals(second.key(), loadedFirst.destination());
        assertEquals(TeleportPadDirection.SOUTH_WEST, loadedFirst.direction());
        assertEquals("Mine, entrance; b", loadedFirst.name());
        assertEquals(33, loaded.at(-5, 71, 9).icon());
    }

    @Test
    void capIsFiftySix() {
        TeleportPadSet set = new TeleportPadSet();
        for (int i = 0; i < TeleportPadSet.MAX_PADS; i++) assertNotNull(set.add(i, 70, 0));
        assertTrue(set.isFull());
        assertNull(set.add(100, 70, 0));
    }

    @Test
    void removingAPadClearsDestinationsPointingAtIt() {
        TeleportPadSet set = new TeleportPadSet();
        TeleportPad first = set.add(0, 70, 0);
        TeleportPad second = set.add(10, 70, 0);
        first.destination(second.key());
        assertTrue(set.remove(10, 70, 0));
        assertNull(first.destination());
        assertFalse(set.remove(10, 70, 0));
    }

    @Test
    void newPadsTakeTheFirstUnusedIcon() {
        TeleportPadSet set = new TeleportPadSet();
        assertEquals(0, set.add(0, 70, 0).icon());
        assertEquals(1, set.add(1, 70, 0).icon());
    }

    @Test
    void badEntriesAreSkipped() {
        assertEquals(0, TeleportPadSet.parse("garbage;1,2,3").size());
        assertEquals(0, TeleportPadSet.parse(null).size());
    }

    @Test
    void directionYawsMatchMinecraftCompass() {
        assertEquals(180f, TeleportPadDirection.NORTH.yawOr(10f));
        assertEquals(270f, TeleportPadDirection.EAST.yawOr(10f));
        assertEquals(10f, TeleportPadDirection.PLAYER.yawOr(10f));
    }
}
