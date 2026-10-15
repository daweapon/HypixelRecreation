package dev.daweapon.anticheat.event.packet;

import lombok.Getter;

import java.util.UUID;

@Getter
// Loader-agnostic representation of a client's player-abilities packet (flight/creative/invulnerability state).
public class AbilitiesPacket extends AnticheatPacket {
    private final boolean invulnerable;
    private final boolean flying;
    private final boolean allowFlight;
    private final boolean creativeMode;
    private final float flySpeed;
    private final float walkSpeed;

    public AbilitiesPacket(UUID uuid, boolean invulnerable, boolean flying, boolean allowFlight,
                          boolean creativeMode, float flySpeed, float walkSpeed) {
        super(uuid);
        this.invulnerable = invulnerable;
        this.flying = flying;
        this.allowFlight = allowFlight;
        this.creativeMode = creativeMode;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
    }
}
