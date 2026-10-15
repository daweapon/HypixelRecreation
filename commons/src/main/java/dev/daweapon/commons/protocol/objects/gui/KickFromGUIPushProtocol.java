package dev.daweapon.commons.protocol.objects.gui;

import dev.daweapon.commons.protocol.RedisProtocol;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class KickFromGUIPushProtocol
        extends RedisProtocol<KickFromGUIPushProtocol.Request, KickFromGUIPushProtocol.Response> {

    public KickFromGUIPushProtocol() {
        super(Request.class, Response.class);
    }

    public record Request(List<UUID> playerUUIDs, String guiType) {}

    public record Response(boolean success, List<UUID> kickedPlayers, int totalKicked, @Nullable String error) {}
}
