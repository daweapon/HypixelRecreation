package net.swofty.anticheat.event.packet;

import lombok.Getter;
import net.swofty.anticheat.engine.SwoftyPlayer;

@Getter
// Loader-agnostic outgoing packet asking the client to echo back a ping request id, for latency measurement.
public class RequestPingPacket extends SwoftyPacket {
    private final int requestId;

    public RequestPingPacket(SwoftyPlayer player, int requestId) {
        super(player);

        this.requestId = requestId;
    }
}
