package dev.daweapon.type.catacombs.secret;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretLedgerTest {
    @Test
    void eachSecretCountsOnlyOnce() {
        SecretLedger ledger = new SecretLedger(Map.of(3, 2));
        SecretLedger.Key chest = new SecretLedger.Key(3, 0);

        assertTrue(ledger.find(chest));
        assertFalse(ledger.find(chest));
        assertFalse(ledger.find(chest));
        assertEquals(1, ledger.found());
        assertEquals(1, ledger.foundIn(3));
        assertTrue(ledger.isFound(chest));
    }

    @Test
    void aRoomIsDoneOnceAllItsSecretsAreFound() {
        SecretLedger ledger = new SecretLedger(Map.of(3, 2, 4, 0));
        assertFalse(ledger.allFound(3));
        ledger.find(new SecretLedger.Key(3, 0));
        assertFalse(ledger.allFound(3));
        ledger.find(new SecretLedger.Key(3, 1));
        assertTrue(ledger.allFound(3));
        assertEquals(2, ledger.foundIn(3));
        assertTrue(ledger.allFound(4));
    }

    @Test
    void roomsKeepSeparateCounts() {
        SecretLedger ledger = new SecretLedger(Map.of(1, 3, 2, 3));
        ledger.find(new SecretLedger.Key(1, 0));
        ledger.find(new SecretLedger.Key(2, 0));
        ledger.find(new SecretLedger.Key(2, 2));
        assertEquals(1, ledger.foundIn(1));
        assertEquals(2, ledger.foundIn(2));
        assertEquals(3, ledger.found());
        assertEquals("<7>2/3 Secrets", SecretMessages.roomSecrets(ledger.foundIn(2), ledger.totalIn(2)));
    }
}
