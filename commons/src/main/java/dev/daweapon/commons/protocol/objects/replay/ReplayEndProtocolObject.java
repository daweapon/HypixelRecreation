package dev.daweapon.commons.protocol.objects.replay;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;

public class ReplayEndProtocolObject extends RedisProtocol<
        ReplayEndProtocolObject.EndMessage,
        ReplayEndProtocolObject.EndResponse> {

    @Override
    public Serializer<EndMessage> getSerializer() {
        return new JacksonSerializer<>(EndMessage.class);
    }

    @Override
    public Serializer<EndResponse> getReturnSerializer() {
        return new JacksonSerializer<>(EndResponse.class);
    }

    public record EndMessage(
            UUID replayId,
            long endTime,
            int durationTicks
    ) {}

    public record EndResponse(
            boolean success,
            long totalBytes,
            long compressedBytes,
            boolean available
    ) {}
}
