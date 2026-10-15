package dev.daweapon.commons.protocol.objects.orchestrator;

import dev.daweapon.commons.UnderstandableProxyServer;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class ChooseGameProtocol extends RedisProtocol
        <ChooseGameProtocol.ChooseGameMessage,
                ChooseGameProtocol.ChooseGameResponse> {
    private static final Serializer<ChooseGameMessage> SERIALIZER =
            new JacksonSerializer<>(ChooseGameMessage.class);
    private static final Serializer<ChooseGameResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(ChooseGameResponse.class);

    @Override

    public Serializer<ChooseGameMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<ChooseGameResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record ChooseGameMessage(UUID player, UnderstandableProxyServer server, String gameId) {
    }

    public record ChooseGameResponse(boolean success, @Nullable String error) {
    }
}
