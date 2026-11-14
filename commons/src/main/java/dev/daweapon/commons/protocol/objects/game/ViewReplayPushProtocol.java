package dev.daweapon.commons.protocol.objects.game;

import dev.daweapon.commons.protocol.RedisProtocol;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ViewReplayPushProtocol
        extends RedisProtocol<ViewReplayPushProtocol.Request, ViewReplayPushProtocol.Response> {

    public ViewReplayPushProtocol() {
        super(Request.class, Response.class);
    }

    public record Request(UUID uuid, String replayId, @Nullable String shareCode) {}

    public record Response(boolean success) {}
}
