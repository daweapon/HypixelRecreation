package dev.daweapon.commons.protocol.objects.admin;

import dev.daweapon.commons.protocol.RedisProtocol;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Broadcast from the control API to every game server; only the server the player is connected to
// acts on it and answers handled = true, so the caller knows whether the player was online anywhere.
public class AdminPlayerActionProtocol extends RedisProtocol<
        AdminPlayerActionProtocol.ActionRequest,
        AdminPlayerActionProtocol.ActionResponse> {

    public static final String SET_RANK = "SET_RANK";
    public static final String KICK = "KICK";

    public AdminPlayerActionProtocol() {
        super(ActionRequest.class, ActionResponse.class);
    }

    public record ActionRequest(UUID uuid, String action, @Nullable String value) {}

    public record ActionResponse(boolean handled, String detail) {}
}
