package dev.daweapon.commons.protocol.objects.replay;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;

public class ReplayDataBatchProtocolObject extends RedisProtocol<
        ReplayDataBatchProtocolObject.BatchMessage,
        ReplayDataBatchProtocolObject.BatchResponse> {

    @Override
    public Serializer<BatchMessage> getSerializer() {
        return new JacksonSerializer<>(BatchMessage.class);
    }

    @Override
    public Serializer<BatchResponse> getReturnSerializer() {
        return new JacksonSerializer<>(BatchResponse.class);
    }

    public record BatchMessage(
            UUID replayId,
            int batchIndex,
            int startTick,
            int endTick,
            int recordableCount,
            byte[] compressedData
    ) {}

    public record BatchResponse(boolean success, long bytesReceived) {}
}
