package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.ServerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobTheFishTest {
    @Test
    void everyTwentyFiveHundredthKillIsGuaranteed() {
        assertFalse(MobTheFish.pityDue(2499));
        assertTrue(MobTheFish.pityDue(2500));
    }

    @Test
    void appliesOnMoongladeMarshAndTorrhusOnly() {
        assertTrue(MobTheFish.appliesOn(ServerType.SKYBLOCK_GALATEA));
        assertTrue(MobTheFish.appliesOn(ServerType.SKYBLOCK_TORRHUS_CANYON));
        assertFalse(MobTheFish.appliesOn(ServerType.SKYBLOCK_HUB));
        assertFalse(MobTheFish.appliesOn(null));
    }
}
