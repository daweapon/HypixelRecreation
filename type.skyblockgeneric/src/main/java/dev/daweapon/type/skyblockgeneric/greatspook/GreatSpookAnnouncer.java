package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.skyblockgeneric.SkyBlockGenericLoader;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GreatSpookAnnouncer {
    private static final long CHECK_MILLIS = 2_000;
    private static final String STARTED = "<5><l>THE GREAT SPOOK <r><d>has begun! Something sinister stirs in the Hub...";
    private static final String ENDED = "<5><l>THE GREAT SPOOK <r><d>has ended. Until next year...";

    private static Boolean announced;

    private GreatSpookAnnouncer() {
    }

    public static void initialize() {
        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(CHECK_MILLIS);
                } catch (InterruptedException interrupted) {
                    return;
                }
                check(GreatSpookEvent.isActive());
            }
        });
    }

    static synchronized void check(boolean active) {
        if (announced == null) {
            announced = active;
            return;
        }
        if (announced == active) return;
        announced = active;
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) player.sendMessage(active ? STARTED : ENDED);
    }
}
