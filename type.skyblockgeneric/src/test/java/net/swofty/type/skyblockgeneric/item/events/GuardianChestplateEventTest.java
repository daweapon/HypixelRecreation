package net.swofty.type.skyblockgeneric.item.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardianChestplateEventTest {
    @Test
    void blocksOnlyAtFullHealthAndOffCooldown() {
        long now = 1_000_000;
        assertTrue(GuardianChestplateEvent.canBlock(true, now - 60_000, now));
        assertFalse(GuardianChestplateEvent.canBlock(true, now - 59_999, now));
        assertFalse(GuardianChestplateEvent.canBlock(false, 0, now));
        assertTrue(GuardianChestplateEvent.canBlock(true, Long.MIN_VALUE / 2, now));
    }
}
