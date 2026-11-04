package dev.daweapon.commons.protocol.objects.game;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.RedisProtocol;

import java.util.List;
import java.util.UUID;

public class QueuePositionPushProtocol
        extends RedisProtocol<QueuePositionPushProtocol.Request, QueuePositionPushProtocol.Response> {

    public QueuePositionPushProtocol() {
        super(Request.class, Response.class);
    }

    public record Request(List<UUID> players, ServerType type, String activity, int position, int length) {
        public Request {
            players = players == null ? List.of() : List.copyOf(players);
        }
    }

    public record Response(int delivered) {}
}
