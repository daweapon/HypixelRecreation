package net.swofty.type.skyblockgeneric.region.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TempleLockdownTest {
    @Test
    void lockdownCapsSpeedAtTheWalkingLimit() {
        assertEquals(TempleLockdown.SPEED_LIMIT, TempleLockdown.limitSpeed(true, 400));
        assertEquals(50, TempleLockdown.limitSpeed(true, 50));
    }

    @Test
    void speedIsUntouchedOutsideTheTemple() {
        assertEquals(400, TempleLockdown.limitSpeed(false, 400));
    }
}
