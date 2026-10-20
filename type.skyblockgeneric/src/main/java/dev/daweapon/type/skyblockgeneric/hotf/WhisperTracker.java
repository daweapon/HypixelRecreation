package dev.daweapon.type.skyblockgeneric.hotf;

import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WhisperTracker {
    public static final long VISIBLE_MILLIS = 30_000;

    private static final Map<UUID, Snapshot> SESSIONS = new ConcurrentHashMap<>();

    private WhisperTracker() {
    }

    public record Snapshot(long total, long delta, long at) {
    }

    public static void record(SkyBlockPlayer player, long amount) {
        if (amount <= 0) return;
        SESSIONS.merge(player.getUuid(), new Snapshot(amount, amount, System.currentTimeMillis()),
                (old, gain) -> new Snapshot(old.total() + gain.delta(), gain.delta(), gain.at()));
    }

    public static @Nullable Snapshot recent(SkyBlockPlayer player, long now) {
        Snapshot snapshot = SESSIONS.get(player.getUuid());
        return snapshot != null && now - snapshot.at() <= VISIBLE_MILLIS ? snapshot : null;
    }

    public static void clear(SkyBlockPlayer player) {
        SESSIONS.remove(player.getUuid());
    }
}
