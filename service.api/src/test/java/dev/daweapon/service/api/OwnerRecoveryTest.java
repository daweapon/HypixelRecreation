package dev.daweapon.service.api;

import dev.daweapon.service.api.control.ControlRole;
import dev.daweapon.service.api.control.DeviceRecord;
import dev.daweapon.service.api.control.OwnerRecovery;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnerRecoveryTest {
    private static final Instant NOW = Instant.parse("2026-10-03T12:00:00Z");
    private static final Duration WINDOW = Duration.ofHours(72);

    private static DeviceRecord device(ControlRole role, UUID player, Instant lastSeen, boolean revoked) {
        return new DeviceRecord(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                "device-" + role, "player", player, role, NOW.minus(Duration.ofDays(30)),
                lastSeen, revoked);
    }

    @Test
    void bootstrapsWhenNoOwnerHasEverExisted() {
        OwnerRecovery.Decision decision = OwnerRecovery.assess(UUID.randomUUID(),
                List.of(device(ControlRole.READ_ONLY, UUID.randomUUID(), NOW, false)), NOW, WINDOW);

        assertEquals(OwnerRecovery.Verdict.BOOTSTRAP, decision.verdict());
        assertTrue(decision.allowed());
    }

    @Test
    void samePlayerReplacesTheirOwnLiveOwnerDevice() {
        UUID me = UUID.randomUUID();
        DeviceRecord mine = device(ControlRole.OWNER, me, NOW, false);

        OwnerRecovery.Decision decision = OwnerRecovery.assess(me, List.of(mine), NOW, WINDOW);

        assertEquals(OwnerRecovery.Verdict.SAME_PLAYER, decision.verdict());
        assertEquals(List.of(mine), decision.supersededDevices());
    }

    @Test
    void refusesWhileSomebodyElsesOwnerDeviceIsStillActive() {
        DeviceRecord theirs = device(ControlRole.OWNER, UUID.randomUUID(),
                NOW.minus(Duration.ofHours(1)), false);

        OwnerRecovery.Decision decision = OwnerRecovery.assess(UUID.randomUUID(), List.of(theirs),
                NOW, WINDOW);

        assertEquals(OwnerRecovery.Verdict.REFUSED, decision.verdict());
        assertFalse(decision.allowed());
        assertTrue(decision.reason().contains("promote you"));
    }

    @Test
    void allowsOnceEveryOwnerDeviceHasGoneQuietPastTheWindow() {
        DeviceRecord theirs = device(ControlRole.OWNER, UUID.randomUUID(),
                NOW.minus(Duration.ofHours(73)), false);

        OwnerRecovery.Decision decision = OwnerRecovery.assess(UUID.randomUUID(), List.of(theirs),
                NOW, WINDOW);

        assertEquals(OwnerRecovery.Verdict.NO_LIVE_OWNER, decision.verdict());
        assertTrue(decision.supersededDevices().isEmpty(),
                "another player's device must not be revoked out from under them");
    }

    @Test
    void theMostRecentlyActiveOwnerDecidesLiveness() {
        DeviceRecord stale = device(ControlRole.OWNER, UUID.randomUUID(),
                NOW.minus(Duration.ofDays(20)), false);
        DeviceRecord active = device(ControlRole.OWNER, UUID.randomUUID(),
                NOW.minus(Duration.ofMinutes(5)), false);

        assertEquals(OwnerRecovery.Verdict.REFUSED,
                OwnerRecovery.assess(UUID.randomUUID(), List.of(stale, active), NOW, WINDOW).verdict());
    }

    @Test
    void revokedOwnerDevicesDoNotBlockRecovery() {
        DeviceRecord revoked = device(ControlRole.OWNER, UUID.randomUUID(), NOW, true);

        assertEquals(OwnerRecovery.Verdict.BOOTSTRAP,
                OwnerRecovery.assess(UUID.randomUUID(), List.of(revoked), NOW, WINDOW).verdict());
    }

    @Test
    void aDeviceThatNeverCheckedInFallsBackToItsCreationTime() {
        DeviceRecord neverUsed = new DeviceRecord("hash", "id", "unused", "player",
                UUID.randomUUID(), ControlRole.OWNER, NOW.minus(Duration.ofHours(80)), null, false);

        assertEquals(OwnerRecovery.Verdict.NO_LIVE_OWNER,
                OwnerRecovery.assess(UUID.randomUUID(), List.of(neverUsed), NOW, WINDOW).verdict());
    }
}
