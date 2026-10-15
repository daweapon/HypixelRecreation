package dev.daweapon.commons.protocol.objects.presence;

import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

public class UpdatePresenceProtocol extends RedisProtocol<
        UpdatePresenceProtocol.UpdatePresenceMessage,
        UpdatePresenceProtocol.UpdatePresenceResponse> {
    private static final Serializer<UpdatePresenceMessage> SERIALIZER =
            new JacksonSerializer<>(UpdatePresenceMessage.class);
    private static final Serializer<UpdatePresenceResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(UpdatePresenceResponse.class);

    @Override
    public Serializer<UpdatePresenceMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<UpdatePresenceResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record UpdatePresenceMessage(PresenceInfo presence) {}

    public record UpdatePresenceResponse(boolean success, @Nullable String error) {}
}
