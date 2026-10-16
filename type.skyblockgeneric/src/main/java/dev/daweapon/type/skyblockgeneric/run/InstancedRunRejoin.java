package dev.daweapon.type.skyblockgeneric.run;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.objects.orchestrator.RejoinGameProtocol;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InstancedRunRejoin {
    public static final long RETRY_COOLDOWN_MILLIS = 60_000;
    private static final Set<ServerType> RUN_SERVERS = Set.of(ServerType.SKYBLOCK_CATACOMBS, ServerType.SKYBLOCK_KUUDRA);
    private static final Map<UUID, Long> LAST_ATTEMPT = new ConcurrentHashMap<>();

    private InstancedRunRejoin() {}

    public static boolean isRunServer(ServerType type) {
        return RUN_SERVERS.contains(type);
    }

    public static boolean shouldRejoin(RejoinGameProtocol.RejoinGameResponse response) {
        return response != null && response.hasActiveGame() && response.server() != null && isRunServer(response.server().type());
    }

    public static boolean tryClaim(UUID player, long now) {
        boolean[] claimed = {false};
        LAST_ATTEMPT.compute(player, (uuid, last) -> {
            if (last != null && now - last < RETRY_COOLDOWN_MILLIS) return last;
            claimed[0] = true;
            return now;
        });
        return claimed[0];
    }

    public static void release(UUID player) {
        LAST_ATTEMPT.remove(player);
    }
}
