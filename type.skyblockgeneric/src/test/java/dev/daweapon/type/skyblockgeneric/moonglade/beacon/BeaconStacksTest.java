package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeaconStacksTest {
    @Test
    void stacksExpireIndividually() {
        BeaconStacks stacks = new BeaconStacks();
        stacks.add(1_000);
        stacks.add(5_000);
        assertEquals(2, stacks.active(0));
        assertEquals(1, stacks.active(1_000));
        assertEquals(0, stacks.active(5_000));
    }

    @Test
    void reportsTimeUntilNextExpiry() {
        BeaconStacks stacks = new BeaconStacks();
        assertEquals(0, stacks.millisUntilNextExpiry(0));
        stacks.add(9_000);
        stacks.add(4_000);
        assertEquals(4_000, stacks.millisUntilNextExpiry(0));
        stacks.clear();
        assertEquals(0, stacks.active(0));
    }

    @Test
    void stackLifetimeIsTwentyMinutes() {
        assertEquals(1_200_000L, BeaconRules.STACK_MILLIS);
    }
}
