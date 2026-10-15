package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.layout.DoorType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeysAndDoorsTest {
    @Test
    void plainDoorsNeedNoKey() {
        KeyRing keys = new KeyRing();
        assertEquals(Optional.empty(), DoorRules.requiredKey(DoorType.PLAIN));
        assertTrue(DoorRules.canOpen(DoorType.PLAIN, keys));
        assertTrue(DoorRules.tryOpen(DoorType.PLAIN, keys));
    }

    @Test
    void witherDoorNeedsAWitherKeyAndSpendsIt() {
        KeyRing keys = new KeyRing();
        assertFalse(DoorRules.canOpen(DoorType.WITHER, keys));
        assertFalse(DoorRules.tryOpen(DoorType.WITHER, keys));
        keys.grant(KeyType.WITHER);
        assertTrue(DoorRules.canOpen(DoorType.WITHER, keys));
        assertTrue(DoorRules.tryOpen(DoorType.WITHER, keys));
        assertFalse(keys.has(KeyType.WITHER));
        assertFalse(DoorRules.tryOpen(DoorType.WITHER, keys));
    }

    @Test
    void bloodDoorNeedsTheBloodKeyNotAWitherKey() {
        KeyRing keys = new KeyRing();
        keys.grant(KeyType.WITHER);
        assertFalse(DoorRules.canOpen(DoorType.BLOOD, keys));
        keys.grant(KeyType.BLOOD);
        assertTrue(DoorRules.tryOpen(DoorType.BLOOD, keys));
        assertTrue(keys.has(KeyType.WITHER));
        assertFalse(keys.has(KeyType.BLOOD));
    }

    @Test
    void witherKeysStack() {
        KeyRing keys = new KeyRing();
        keys.grant(KeyType.WITHER);
        keys.grant(KeyType.WITHER);
        assertEquals(2, keys.count(KeyType.WITHER));
        keys.consume(KeyType.WITHER);
        assertEquals(1, keys.count(KeyType.WITHER));
    }

    @Test
    void enteringTheBloodRoomWithoutOpeningItsDoorFailsTheRun() {
        assertEquals(BloodRoomGate.Verdict.FAILS_RUN, BloodRoomGate.onEnter(false));
        assertEquals(BloodRoomGate.Verdict.ALLOWED, BloodRoomGate.onEnter(true));
    }

    @Test
    void eachKeyTypeOpensItsOwnDoorType() {
        assertEquals(DoorType.WITHER, KeyType.WITHER.door());
        assertEquals(DoorType.BLOOD, KeyType.BLOOD.door());
    }
}
