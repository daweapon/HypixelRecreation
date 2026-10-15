package dev.daweapon.type.bedwarsgame.util;

import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorUtilTest {
    @Test
    void textColorLookupIsCaseInsensitive() {
        assertEquals(NamedTextColor.GOLD, ColorUtil.getTextColorByName("gold"));
        assertEquals(NamedTextColor.DARK_GREEN, ColorUtil.getTextColorByName("Dark_Green"));
    }

    @Test
    void awtColorLookupResolvesConstants() {
        assertEquals(java.awt.Color.RED, ColorUtil.getColorByName("red"));
    }

    @Test
    void unknownNamesReturnNull() {
        assertNull(ColorUtil.getTextColorByName("not_a_color"));
        assertNull(ColorUtil.getColorByName("not_a_color"));
    }
}
