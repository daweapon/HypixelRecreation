package dev.daweapon.type.skyblockgeneric.gui.inventories.bazaar;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The service ping only waits 150 ms, so one slow reply over Redis is not an outage; an open Bazaar menu
 * closes only after several missed pings in a row.
 */
final class BazaarOfflineWatch {
    static final int MISSES_BEFORE_OFFLINE = 3;
    private final AtomicInteger misses = new AtomicInteger();

    boolean isOffline(boolean online) {
        if (online) {
            misses.set(0);
            return false;
        }
        return misses.incrementAndGet() >= MISSES_BEFORE_OFFLINE;
    }
}
