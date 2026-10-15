package dev.daweapon.commons.protocol.objects.presence;

import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class GetPresenceBulkProtocol extends RedisProtocol<
        GetPresenceBulkProtocol.GetPresenceBulkMessage,
        GetPresenceBulkProtocol.GetPresenceBulkResponse> {
    private static final Serializer<GetPresenceBulkMessage> SERIALIZER =
            new JacksonSerializer<>(GetPresenceBulkMessage.class);
    private static final Serializer<GetPresenceBulkResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(GetPresenceBulkResponse.class);

    @Override
    public Serializer<GetPresenceBulkMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<GetPresenceBulkResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record GetPresenceBulkMessage(List<UUID> uuids) {}

    public record GetPresenceBulkResponse(List<PresenceInfo> presence, boolean success, @Nullable String error) {}
}
