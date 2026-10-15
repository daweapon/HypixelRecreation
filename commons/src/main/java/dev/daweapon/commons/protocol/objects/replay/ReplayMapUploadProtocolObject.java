package dev.daweapon.commons.protocol.objects.replay;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

public class ReplayMapUploadProtocolObject extends RedisProtocol<
        ReplayMapUploadProtocolObject.MapUploadMessage,
        ReplayMapUploadProtocolObject.MapUploadResponse> {

    @Override
    public Serializer<MapUploadMessage> getSerializer() {
        return new JacksonSerializer<>(MapUploadMessage.class);
    }

    @Override
    public Serializer<MapUploadResponse> getReturnSerializer() {
        return new JacksonSerializer<>(MapUploadResponse.class);
    }

    public record MapUploadMessage(
            String mapHash,
            String mapName,
            byte[] compressedData
    ) {}

    public record MapUploadResponse(boolean success, boolean alreadyExists) {}
}
