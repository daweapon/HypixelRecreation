package dev.daweapon.commons.protocol.objects.replay;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

public class ReplayMapLoadProtocolObject extends RedisProtocol<
    ReplayMapLoadProtocolObject.MapLoadRequest,
    ReplayMapLoadProtocolObject.MapLoadResponse> {
    @Override
    public Serializer<MapLoadRequest> getSerializer() {
        return new JacksonSerializer<>(MapLoadRequest.class);
    }

    @Override
    public Serializer<MapLoadResponse> getReturnSerializer() {
        return new JacksonSerializer<>(MapLoadResponse.class);
    }

    public record MapLoadRequest(String mapHash) {
    }

    public record MapLoadResponse(
        boolean success,
        boolean found,
        String mapHash,
        byte[] compressedData,
        int uncompressedSize
    ) {
    }
}
