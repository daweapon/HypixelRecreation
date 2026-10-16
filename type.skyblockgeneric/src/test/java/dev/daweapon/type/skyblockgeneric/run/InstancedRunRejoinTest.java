package dev.daweapon.type.skyblockgeneric.run;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.UnderstandableProxyServer;
import dev.daweapon.commons.protocol.objects.orchestrator.RejoinGameProtocol.RejoinGameResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstancedRunRejoinTest {
    private static RejoinGameResponse response(boolean active, ServerType type) {
        UnderstandableProxyServer server = type == null ? null
                : new UnderstandableProxyServer("mini1", UUID.randomUUID(), type, -1, new ArrayList<>(), 10, "mini1");
        return new RejoinGameResponse(active, server, "game", "map", null, false, true, null);
    }

    @Test
    void catacombsAndKuudraRunsAreRejoined() {
        assertTrue(InstancedRunRejoin.shouldRejoin(response(true, ServerType.SKYBLOCK_CATACOMBS)));
        assertTrue(InstancedRunRejoin.shouldRejoin(response(true, ServerType.SKYBLOCK_KUUDRA)));
    }

    @Test
    void otherGamesAndMissingGamesAreIgnored() {
        assertFalse(InstancedRunRejoin.shouldRejoin(response(true, ServerType.BEDWARS_GAME)));
        assertFalse(InstancedRunRejoin.shouldRejoin(response(false, ServerType.SKYBLOCK_CATACOMBS)));
        assertFalse(InstancedRunRejoin.shouldRejoin(response(true, null)));
        assertFalse(InstancedRunRejoin.shouldRejoin(null));
    }

    @Test
    void aPlayerIsOnlySentOnceAMinuteUnlessTheCheckFindsNothing() {
        UUID player = UUID.randomUUID();
        assertTrue(InstancedRunRejoin.tryClaim(player, 1_000));
        assertFalse(InstancedRunRejoin.tryClaim(player, 30_000));
        assertTrue(InstancedRunRejoin.tryClaim(player, 1_000 + InstancedRunRejoin.RETRY_COOLDOWN_MILLIS));
        InstancedRunRejoin.release(player);
        assertTrue(InstancedRunRejoin.tryClaim(player, 1_001));
    }

    @Test
    void runServersAreIdentified() {
        assertTrue(InstancedRunRejoin.isRunServer(ServerType.SKYBLOCK_CATACOMBS));
        assertFalse(InstancedRunRejoin.isRunServer(ServerType.SKYBLOCK_HUB));
    }
}
