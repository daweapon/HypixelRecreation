package dev.daweapon.type.skyblockgeneric.farming.jake;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JakeShop {
    public static final long OPEN_MILLIS = 10 * 60 * 1000L;

    private static final Map<String, Long> OPEN_UNTIL = new ConcurrentHashMap<>();

    private JakeShop() {
    }

    public static void open(String itemId) {
        OPEN_UNTIL.put(itemId, System.currentTimeMillis() + OPEN_MILLIS);
    }

    public static boolean isOpen(String itemId) {
        Long until = OPEN_UNTIL.get(itemId);
        if (until == null) return false;
        if (until > System.currentTimeMillis()) return true;
        OPEN_UNTIL.remove(itemId, until);
        return false;
    }

    public static boolean anyOpen() {
        OPEN_UNTIL.keySet().forEach(JakeShop::isOpen);
        return !OPEN_UNTIL.isEmpty();
    }

    public static void closeAll() {
        OPEN_UNTIL.clear();
    }
}
