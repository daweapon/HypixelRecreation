package dev.daweapon.service.replay.endpoints;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.protocol.objects.replay.ReplayListProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.replay.ReplayService;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Redis endpoint that lists a player's stored replays as summaries.
public class ReplayListEndpoint implements RedisMessageHandler<
        ReplayListProtocolObject.ListRequest,
        ReplayListProtocolObject.ListResponse> {

    @Override
    public ReplayListProtocolObject protocol() {
        return new ReplayListProtocolObject();
    }

    @Override
    public ReplayListProtocolObject.ListResponse handle(ReplayListProtocolObject.ListRequest msg, RedisMessageContext context) {

        try {
            List<Document> docs = ReplayService.getDatabase().getReplaysByPlayer(msg.playerId(), msg.limit());

            List<ReplayListProtocolObject.ReplaySummary> replays = new ArrayList<>();
            for (Document doc : docs) {
                Map<UUID, String> players = new HashMap<>();
                Document playerNames = doc.get("playerNames", Document.class);
                if (playerNames != null) {
                    playerNames.forEach((key, value) -> players.put(UUID.fromString(key), (String) value));
                }

                replays.add(new ReplayListProtocolObject.ReplaySummary(
                        UUID.fromString(doc.getString("replayId")),
                        doc.getString("gameId"),
                        ServerType.valueOf(doc.getString("serverType")),
                        doc.getString("serverId"),
                        doc.getString("gameTypeName"),
                        doc.getString("mapName"),
                        doc.getLong("startTime"),
                        doc.getLong("endTime"),
                        doc.getInteger("durationTicks"),
                        players.size(),
                        players,
                        doc.getString("winnerId"),
                        doc.getString("winnerType"),
                        doc.getLong("dataSize")
                ));
            }

            Logger.debug("Returning {} replays for player {}", replays.size(), msg.playerId());
            return new ReplayListProtocolObject.ListResponse(true, replays);

        } catch (Exception e) {
            Logger.error(e, "Failed to fetch replays for player {}", msg.playerId());
            return new ReplayListProtocolObject.ListResponse(false, List.of());
        }
    }
}
