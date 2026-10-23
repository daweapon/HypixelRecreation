package dev.daweapon.type.skyblockgeneric.dante;

import java.util.Set;

public final class DanteEventCancellation {
    public static final Set<String> CANCELLED_EVENT_IDS = Set.of("traveling_zoo", "spooky_festival", "season_of_jerry");

    private DanteEventCancellation() {
    }

    public static boolean cancels(String eventId) {
        return CANCELLED_EVENT_IDS.contains(eventId) && DanteAccess.securityActive();
    }
}
