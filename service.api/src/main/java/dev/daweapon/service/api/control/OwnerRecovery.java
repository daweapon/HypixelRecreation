package dev.daweapon.service.api.control;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Decides whether a verified in-game player may claim OWNER without an existing owner device.
 *
 * Without this the control plane has a terminal failure mode: roles can only be granted by an
 * owner, so losing the sole owner device means no device can ever be promoted again and the
 * only way back in is editing Mongo by hand - which needs exactly the shell access this app
 * exists to avoid needing.
 *
 * Authority comes from the in-game handshake, which is STAFF-gated, and the decision is kept
 * free of Mongo and HTTP so the rules can be tested directly.
 */
public final class OwnerRecovery {
    public static final Duration DEFAULT_IDLE_WINDOW = Duration.ofHours(72);

    public enum Verdict {
        /** The claimant already owns a live owner device; they are replacing their own hardware. */
        SAME_PLAYER,
        /** No owner device has checked in recently, so the owner set is presumed lost. */
        NO_LIVE_OWNER,
        /** No owner device has ever existed. */
        BOOTSTRAP,
        /** Somebody else's owner device is still active; they can promote the claimant instead. */
        REFUSED
    }

    public record Decision(Verdict verdict, String reason, List<DeviceRecord> supersededDevices) {
        public boolean allowed() {
            return verdict != Verdict.REFUSED;
        }
    }

    private OwnerRecovery() {
    }

    public static Duration configuredIdleWindow() {
        String raw = System.getenv("CONTROL_OWNER_RECOVERY_IDLE_HOURS");
        if (raw == null || raw.isBlank()) {
            return DEFAULT_IDLE_WINDOW;
        }
        try {
            long hours = Long.parseLong(raw.trim());
            return hours <= 0 ? DEFAULT_IDLE_WINDOW : Duration.ofHours(hours);
        } catch (NumberFormatException e) {
            return DEFAULT_IDLE_WINDOW;
        }
    }

    public static Decision assess(UUID claimantUuid, List<DeviceRecord> devices, Instant now,
                                  Duration idleWindow) {
        List<DeviceRecord> owners = new ArrayList<>();
        for (DeviceRecord device : devices) {
            if (device.role() == ControlRole.OWNER && !device.revoked()) {
                owners.add(device);
            }
        }

        if (owners.isEmpty()) {
            return new Decision(Verdict.BOOTSTRAP,
                    "no owner device exists", List.of());
        }

        List<DeviceRecord> claimantOwned = new ArrayList<>();
        for (DeviceRecord owner : owners) {
            if (claimantUuid != null && claimantUuid.equals(owner.playerUUID())) {
                claimantOwned.add(owner);
            }
        }
        if (!claimantOwned.isEmpty()) {
            return new Decision(Verdict.SAME_PLAYER,
                    "claimant already owns " + claimantOwned.size() + " owner device(s)",
                    claimantOwned);
        }

        Instant cutoff = now.minus(idleWindow);
        DeviceRecord mostRecent = null;
        for (DeviceRecord owner : owners) {
            if (mostRecent == null || lastActive(owner).isAfter(lastActive(mostRecent))) {
                mostRecent = owner;
            }
        }
        if (lastActive(mostRecent).isAfter(cutoff)) {
            return new Decision(Verdict.REFUSED,
                    "owner device '" + mostRecent.deviceName() + "' belonging to "
                            + mostRecent.playerName() + " was last seen "
                            + lastActive(mostRecent) + "; ask them to promote you from the app",
                    List.of());
        }

        return new Decision(Verdict.NO_LIVE_OWNER,
                "no owner device seen since " + lastActive(mostRecent), List.of());
    }

    /** Devices enrolled but never used still have a creation time, which is a fair liveness floor. */
    static Instant lastActive(DeviceRecord device) {
        return device.lastSeenAt() == null ? device.createdAt() : device.lastSeenAt();
    }
}
