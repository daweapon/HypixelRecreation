package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.UnderstandableProxyServer;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

public class GetServerForMapProtocol extends RedisProtocol
        <GetServerForMapProtocol.GetServerForMapMessage,
                GetServerForMapProtocol.GetServerForMapResponse> {
    private static final Serializer<GetServerForMapMessage> SERIALIZER =
            new JacksonSerializer<>(GetServerForMapMessage.class);
    private static final Serializer<GetServerForMapResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(GetServerForMapResponse.class);

    @Override

    public Serializer<GetServerForMapMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<GetServerForMapResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record GetServerForMapMessage(ServerType type, @Nullable String map, String mode, int neededSlots) {
    }

    public record GetServerForMapResponse(UnderstandableProxyServer server, String gameId, boolean success,
                                          @Nullable String error, @Nullable String reason) {
        public GetServerForMapResponse(UnderstandableProxyServer server, String gameId, boolean success, @Nullable String error) {
            this(server, gameId, success, error, null);
        }
    }
}
