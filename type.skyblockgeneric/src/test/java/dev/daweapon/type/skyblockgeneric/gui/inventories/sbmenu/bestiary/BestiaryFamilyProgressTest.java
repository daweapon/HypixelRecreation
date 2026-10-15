package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BestiaryFamilyProgressTest {

    @Test
    void percentagesMatchHypixelsOneDecimalFormat() {
        assertEquals("95.8", BestiaryFamilyProgress.percent(23, 24));
        assertEquals("12.5", BestiaryFamilyProgress.percent(3, 24));
        assertEquals("78.9", BestiaryFamilyProgress.percent(15, 19));
        assertEquals("21.1", BestiaryFamilyProgress.percent(4, 19));
        assertEquals("100", BestiaryFamilyProgress.percent(6, 6));
        assertEquals("0", BestiaryFamilyProgress.percent(0, 6));
    }
}
