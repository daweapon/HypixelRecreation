package dev.daweapon.commons.protocol.objects.game;

import dev.daweapon.commons.protocol.RedisProtocol;

public class DrainServerPushProtocol
        extends RedisProtocol<DrainServerPushProtocol.Request, DrainServerPushProtocol.Response> {

    public DrainServerPushProtocol() {
        super(Request.class, Response.class);
    }

    public record Request(String reason) {}

    public record Response(int players, String destination) {}
}
