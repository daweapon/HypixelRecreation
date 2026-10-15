package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.UnderstandableProxyServer;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RejoinGameProtocol extends RedisProtocol<
        RejoinGameProtocol.RejoinGameRequest,
        RejoinGameProtocol.RejoinGameResponse> {
    private static final Serializer<RejoinGameRequest> SERIALIZER =
            new JacksonSerializer<>(RejoinGameRequest.class);
    private static final Serializer<RejoinGameResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(RejoinGameResponse.class);

    @Override
    public Serializer<RejoinGameRequest> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<RejoinGameResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record RejoinGameRequest(UUID playerUuid) {
    }

    public record RejoinGameResponse(
            boolean hasActiveGame,
            @Nullable UnderstandableProxyServer server,
            @Nullable String gameId,
            @Nullable String mapName,
            @Nullable String teamName,
            boolean willBeSpectator,
            boolean success,
            @Nullable String error
    ) {
    }
}
