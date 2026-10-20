package dev.daweapon.type.skyblockgeneric.potion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoffeeBaseTest {
    @Test
    void coffeesGiveTheWikiSpeed() {
        assertEquals(5, CoffeeBase.of("CHEAP_COFFEE").speed());
        assertEquals(8, CoffeeBase.of("DECENT_COFFEE").speed());
        assertEquals(12, CoffeeBase.of("BLACK_COFFEE").speed());
    }

    @Test
    void otherBasesGiveNothing() {
        assertNull(CoffeeBase.of(null));
        assertNull(CoffeeBase.of("AWKWARD"));
    }
}
