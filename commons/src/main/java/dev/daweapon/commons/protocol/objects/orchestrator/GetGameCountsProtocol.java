package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

public class GetGameCountsProtocol extends RedisProtocol
        <GetGameCountsProtocol.GetGameCountsMessage,
                GetGameCountsProtocol.GetGameCountsResponse> {
    private static final Serializer<GetGameCountsMessage> SERIALIZER =
            new JacksonSerializer<>(GetGameCountsMessage.class);
    private static final Serializer<GetGameCountsResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(GetGameCountsResponse.class);

    @Override

    public Serializer<GetGameCountsMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<GetGameCountsResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record GetGameCountsMessage(ServerType type, String gameTypeName, String mapName) { }

    public record GetGameCountsResponse(int playerCount, int gameCount, boolean success, @Nullable String error) { }
}
