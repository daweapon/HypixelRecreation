package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;

@Getter
// Loader-agnostic representation of a client's response to an anticheat-issued ping request.
public class PingResponsePacket extends AnticheatPacket {
    private final long requestId;

    public PingResponsePacket(AnticheatPlayer player, long requestId) {
        super(player);

        this.requestId = requestId;
    }
}
