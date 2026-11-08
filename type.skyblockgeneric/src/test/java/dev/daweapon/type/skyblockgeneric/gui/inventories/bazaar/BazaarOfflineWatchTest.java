package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BazaarOfflineWatchTest {
    @Test
    void oneSlowPingDoesNotCloseTheMenu() {
        BazaarOfflineWatch watch = new BazaarOfflineWatch();
        assertFalse(watch.isOffline(false));
        assertFalse(watch.isOffline(false));
        assertFalse(watch.isOffline(true));
        assertFalse(watch.isOffline(false));
    }

    @Test
    void threeMissedPingsInARowMeanOffline() {
        BazaarOfflineWatch watch = new BazaarOfflineWatch();
        watch.isOffline(false);
        watch.isOffline(false);
        assertTrue(watch.isOffline(false));
    }
}
