package dev.daweapon.commons.protocol.objects.api;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class APIAuthenticateCodeProtocol extends RedisProtocol
        <APIAuthenticateCodeProtocol.AuthenticateCodeMessage, APIAuthenticateCodeProtocol.AuthenticateCodeResponse> {
    private static final Serializer<AuthenticateCodeMessage> SERIALIZER =
            new JacksonSerializer<>(AuthenticateCodeMessage.class);
    private static final Serializer<AuthenticateCodeResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(AuthenticateCodeResponse.class);

    @Override

    public Serializer<AuthenticateCodeMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<AuthenticateCodeResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record AuthenticateCodeMessage(String authCode, String playerName, UUID playerUUID) {}

    public record AuthenticateCodeResponse(boolean success, @Nullable String error) {}
}
