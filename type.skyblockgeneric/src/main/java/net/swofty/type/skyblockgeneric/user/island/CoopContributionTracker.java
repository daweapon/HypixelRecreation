package net.swofty.type.skyblockgeneric.user.island;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Real Hypixel tracks per-member mine/farm contribution on a shared island. Accumulating a delta
// per player in memory and flushing periodically avoids taking the coop's distributed roster lock
// on every single block break, which would happen constantly during normal farming/mining.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoopContributionTracker {
    private static final Map<UUID, Long> pendingContributions = new ConcurrentHashMap<>();

    public static void record(UUID playerUuid, long amount) {
        pendingContributions.merge(playerUuid, amount, Long::sum);
    }

    public static void startFlushLoop(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            flush();
            return TaskSchedule.tick(600); // 30 seconds
        });
    }

    private static void flush() {
        if (pendingContributions.isEmpty()) return;

        Map<UUID, Long> snapshot = new HashMap<>(pendingContributions);
        pendingContributions.keySet().removeAll(snapshot.keySet());

        snapshot.forEach((uuid, amount) -> {
            CoopDatabase.Coop coop = CoopDatabase.getFromMember(uuid);
            if (coop == null) return;
            CoopDatabase.update(coop.coopUUID(), latest -> latest.contributions().merge(uuid, amount, Long::sum));
        });
    }
}
