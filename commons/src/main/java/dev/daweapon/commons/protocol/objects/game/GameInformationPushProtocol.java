package dev.daweapon.commons.protocol.objects.game;

import dev.daweapon.commons.protocol.RedisProtocol;

import java.util.UUID;

public class GameInformationPushProtocol
        extends RedisProtocol<GameInformationPushProtocol.Request, GameInformationPushProtocol.Response> {

    public GameInformationPushProtocol() {
        super(Request.class, Response.class);
    }

    public record Request(UUID uuid, String gameId) {}

    public record Response() {}
}
