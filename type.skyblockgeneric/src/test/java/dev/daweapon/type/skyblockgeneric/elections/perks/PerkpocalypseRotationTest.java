package dev.daweapon.type.skyblockgeneric.elections.perks;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PerkpocalypseRotationTest {

    @Test
    void orderHoldsTheSixWikiMayorsOnce() {
        List<SkyBlockMayor> order = PerkpocalypseRotation.order(497);

        assertEquals(6, order.size());
        assertEquals(6, new HashSet<>(order).size());
        assertFalse(order.contains(SkyBlockMayor.DIAZ));
        assertFalse(order.contains(SkyBlockMayor.FOXY));
        assertFalse(order.contains(SkyBlockMayor.BARRY));
    }

    @Test
    void everyServerDerivesTheSameMayorFromTheCalendar() {
        long elapsed = 40L * SkyBlockCalendar.YEAR + 5L * SkyBlockCalendar.DAY;

        assertEquals(PerkpocalypseRotation.order(41), PerkpocalypseRotation.order(41));
        assertEquals(PerkpocalypseRotation.mayorAt(41, elapsed), PerkpocalypseRotation.mayorAt(41, elapsed + 1000));
    }

    @Test
    void mayorChangesEveryEighteenDaysAndCyclesTheWholeOrder() {
        long termStart = 40L * SkyBlockCalendar.YEAR;
        Set<SkyBlockMayor> seen = new HashSet<>();
        SkyBlockMayor previous = null;
        for (int slot = 0; slot < 6; slot++) {
            SkyBlockMayor mayor = PerkpocalypseRotation.mayorAt(41, termStart + slot * PerkpocalypseRotation.SLOT_LENGTH);
            if (previous != null) assertNotEquals(previous, mayor);
            seen.add(mayor);
            previous = mayor;
        }

        assertEquals(6, seen.size());
    }

    @Test
    void slotsRestartAtEachSkyBlockYearToMatchTheCalendarEvent() {
        long lastSlotOfYear = PerkpocalypseRotation.SLOTS_PER_YEAR - 1;

        assertEquals(lastSlotOfYear, PerkpocalypseRotation.slot(SkyBlockCalendar.YEAR - 1));
        assertEquals(PerkpocalypseRotation.SLOTS_PER_YEAR, PerkpocalypseRotation.slot(SkyBlockCalendar.YEAR));
    }
}
