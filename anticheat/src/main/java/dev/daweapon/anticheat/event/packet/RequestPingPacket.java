package dev.daweapon.anticheat.event.packet;

import lombok.Getter;
import dev.daweapon.anticheat.engine.AnticheatPlayer;

@Getter
// Loader-agnostic outgoing packet asking the client to echo back a ping request id, for latency measurement.
public class RequestPingPacket extends AnticheatPacket {
    private final int requestId;

    public RequestPingPacket(AnticheatPlayer player, int requestId) {
        super(player);

        this.requestId = requestId;
    }
}
