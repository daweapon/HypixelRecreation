package dev.daweapon.commons.protocol.objects.replay;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReplayLoadProtocolObject extends RedisProtocol<
        ReplayLoadProtocolObject.LoadRequest,
        ReplayLoadProtocolObject.LoadResponse> {

    @Override
    public Serializer<LoadRequest> getSerializer() {
        return new JacksonSerializer<>(LoadRequest.class);
    }

    @Override
    public Serializer<LoadResponse> getReturnSerializer() {
        return new JacksonSerializer<>(LoadResponse.class);
    }

    public record LoadRequest(UUID replayId) {}

    public record LoadResponse(
            boolean success,
            String errorMessage,
            ReplayMetadata metadata,
            List<DataChunk> dataChunks
    ) {}

    public record ReplayMetadata(
            UUID replayId,
            String gameId,
            ServerType serverType,
            String serverId,
            String gameTypeName,
            String mapName,
            String mapHash,
            int version,
            long startTime,
            long endTime,
            int durationTicks,
            Map<UUID, String> players,
            Map<String, List<UUID>> teams,
            Map<String, TeamInfo> teamInfo,
            String winnerId,
            String winnerType,
            long dataSize,
            double mapCenterX,
            double mapCenterZ
    ) {}

    public record TeamInfo(String name, String colorCode, int color) {}

    public record DataChunk(
            int chunkIndex,
            int startTick,
            int endTick,
            byte[] compressedData
    ) {}
}
