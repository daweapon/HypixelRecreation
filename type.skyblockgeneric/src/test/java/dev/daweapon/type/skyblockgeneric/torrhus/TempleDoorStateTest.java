package dev.daweapon.type.skyblockgeneric.torrhus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TempleDoorStateTest {
    @Test
    void staysClosedUntilUnlocked() {
        TempleDoorState door = new TempleDoorState();
        assertEquals(TempleDoorState.Change.NONE, door.update(false, false, 230));
        assertEquals(TempleDoorState.State.CLOSED, door.state());
        assertEquals(TempleDoorState.Change.OPENED, door.update(true, false, 230));
        assertEquals(TempleDoorState.Change.NONE, door.update(true, false, 230));
    }

    @Test
    void slamsBehindThePlayerAndReopensFromInside() {
        TempleDoorState door = new TempleDoorState();
        door.update(true, false, 230);
        assertEquals(TempleDoorState.Change.NONE, door.update(true, false, 238));
        assertEquals(TempleDoorState.Change.SLAMMED, door.update(true, false, 240));
        assertEquals(TempleDoorState.Change.NONE, door.update(true, false, 237));
        door.update(true, false, 245);
        assertEquals(TempleDoorState.Change.OPENED, door.update(true, false, 237));
    }

    @Test
    void activatedPlayersAlwaysSeeItOpen() {
        TempleDoorState door = new TempleDoorState();
        door.update(true, false, 230);
        door.update(true, false, 241);
        assertEquals(TempleDoorState.State.SLAMMED, door.state());
        assertEquals(TempleDoorState.Change.OPENED, door.update(true, true, 241));
        assertEquals(TempleDoorState.Change.NONE, door.update(true, true, 250));
    }

    @Test
    void relockingClosesIt() {
        TempleDoorState door = new TempleDoorState();
        door.update(true, false, 230);
        assertEquals(TempleDoorState.Change.CLOSED, door.update(false, false, 230));
    }
}
