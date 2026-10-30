package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseAnchorsTest {
    @Test
    void everyOneOfTheTwentyNineLayoutCodesHasSpotsInsideTheShaftBox() {
        assertEquals(29, CorpseAnchors.layoutCodes().size());
        for (String code : CorpseAnchors.layoutCodes()) {
            assertTrue(!CorpseAnchors.spots(code).isEmpty(), code);
            for (Pos spot : CorpseAnchors.spots(code)) {
                assertTrue(spot.x() >= -199 && spot.x() <= -105 && spot.y() >= 0 && spot.y() <= 34 && spot.z() >= -199 && spot.z() <= -153, code + " " + spot);
            }
        }
    }

    @Test
    void crystalLayoutsShareOneSetOfSpots() {
        assertEquals(CorpseAnchors.spots("PERI_C"), CorpseAnchors.spots("RUBY_C"));
        assertEquals(3, CorpseAnchors.spots("OPAL_C").size());
        assertEquals(1, CorpseAnchors.spots("FAIR_1").size());
        assertEquals(4, CorpseAnchors.spots("LITT_L").size());
    }
}
