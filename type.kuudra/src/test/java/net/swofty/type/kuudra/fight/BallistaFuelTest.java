package net.swofty.type.kuudra.fight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BallistaFuelTest {

    @Test
    void aFullLoadFiresOnceThenNeedsRefuelling() {
        BallistaFuel fuel = new BallistaFuel(4);
        assertFalse(fuel.fire());
        for (int i = 0; i < 3; i++) assertEquals(BallistaFuel.Load.LOADED, fuel.load());
        assertEquals(BallistaFuel.Load.FIRST_FULL, fuel.load());
        assertEquals(BallistaFuel.Load.REJECTED, fuel.load());
        assertTrue(fuel.fire());
        assertFalse(fuel.fire());
        assertEquals(0, fuel.loaded());
        assertEquals(4, fuel.missing(0, 0));
        for (int i = 0; i < 3; i++) assertEquals(BallistaFuel.Load.LOADED, fuel.load());
        assertEquals(BallistaFuel.Load.REFUELLED, fuel.load());
        assertTrue(fuel.fire());
        assertTrue(fuel.everLoaded());
    }

    @Test
    void cellsAlreadyOutCountTowardTheLoad() {
        BallistaFuel fuel = new BallistaFuel(4);
        fuel.load();
        assertEquals(1, fuel.missing(1, 1));
        assertEquals(0, fuel.missing(3, 2));
    }
}
