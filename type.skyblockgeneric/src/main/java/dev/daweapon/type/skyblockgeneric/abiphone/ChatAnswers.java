package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class ChatAnswers {
    private static final long WINDOW_MILLIS = 5 * 60_000L;
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private record Pending(Predicate<String> accepts, Runnable onAccepted, long expiresAt) {
    }

    private ChatAnswers() {
    }

    public static void expect(SkyBlockPlayer player, Predicate<String> accepts, Runnable onAccepted) {
        PENDING.put(player.getUuid(), new Pending(accepts, onAccepted, System.currentTimeMillis() + WINDOW_MILLIS));
    }

    public static void observe(SkyBlockPlayer player, String message) {
        Pending pending = PENDING.get(player.getUuid());
        if (pending == null) return;
        if (System.currentTimeMillis() > pending.expiresAt()) {
            PENDING.remove(player.getUuid(), pending);
            return;
        }
        if (!pending.accepts().test(message.trim())) return;
        PENDING.remove(player.getUuid(), pending);
        pending.onAccepted().run();
    }
}
