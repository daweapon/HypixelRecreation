package dev.daweapon.type.skyblockgeneric.crimson;

import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoBelt;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoGrade;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DojoTablesTest {

    @Test
    void gradesFollowTheSharedThresholds() {
        assertEquals(DojoGrade.F, DojoGrade.of(0));
        assertEquals(DojoGrade.F, DojoGrade.of(199));
        assertEquals(DojoGrade.D, DojoGrade.of(200));
        assertEquals(DojoGrade.C, DojoGrade.of(400));
        assertEquals(DojoGrade.B, DojoGrade.of(600));
        assertEquals(DojoGrade.A, DojoGrade.of(999));
        assertEquals(DojoGrade.S, DojoGrade.of(1000));
        assertEquals(DojoGrade.S, DojoGrade.of(1500));
    }

    @Test
    void beltsFollowDojoPoints() {
        assertEquals(DojoBelt.WHITE, DojoBelt.reached(999));
        assertEquals(DojoBelt.YELLOW, DojoBelt.reached(1000));
        assertEquals(DojoBelt.BLUE, DojoBelt.reached(5999));
        assertEquals(DojoBelt.BLACK, DojoBelt.reached(7000));
    }

    @Test
    void beltSkyBlockXPAddsUpToTheDojoTask() {
        assertEquals(0, DojoBelt.cumulativeSkyBlockXP(null));
        assertEquals(20, DojoBelt.cumulativeSkyBlockXP(DojoBelt.WHITE));
        assertEquals(SkyBlockXPSource.THE_DOJO.getMaxXP(), DojoBelt.cumulativeSkyBlockXP(DojoBelt.BLACK));
    }

    @Test
    void testsUnlockInMenuOrder() {
        assertNull(DojoTest.FORCE.previous());
        assertEquals(DojoTest.SWIFTNESS, DojoTest.CONTROL.previous());
        assertEquals(7, DojoTest.values().length);
    }
}
