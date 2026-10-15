package dev.daweapon.service.replay.endpoints;

import dev.daweapon.commons.protocol.objects.replay.ReplayStartProtocolObject;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;
import dev.daweapon.service.replay.ReplayService;
import dev.daweapon.type.game.replay.ReplayMetadata;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

// Redis endpoint that starts a new replay recording session for a game.
public class ReplayStartEndpoint implements RedisMessageHandler<
        ReplayStartProtocolObject.StartMessage,
        ReplayStartProtocolObject.StartResponse> {

    @Override
    public ReplayStartProtocolObject protocol() {
        return new ReplayStartProtocolObject();
    }

    @Override
    public ReplayStartProtocolObject.StartResponse handle(ReplayStartProtocolObject.StartMessage msg, RedisMessageContext context) {

        try {
            Map<String, ReplayMetadata.TeamInfo> teamInfo = new HashMap<>();
            msg.teamInfo().forEach((teamId, info) ->
                    teamInfo.put(teamId, new ReplayMetadata.TeamInfo(info.name(), info.colorCode(), info.color()))
            );

            ReplayService.getSessionManager().startSession(
                    msg.replayId(),
                    msg.gameId(),
                    msg.serverType(),
                    msg.serverId(),
                    msg.gameTypeName(),
                    msg.mapName(),
                    msg.mapHash(),
                    msg.startTime(),
                    msg.mapCenterX(),
                    msg.mapCenterZ(),
                    msg.players(),
                    msg.teams(),
                    teamInfo
            );

            Logger.info("Started replay session {} for game {} with {} players",
                    msg.replayId(), msg.gameId(), msg.players().size());

            return new ReplayStartProtocolObject.StartResponse(true, null);

        } catch (Exception e) {
            Logger.error(e, "Failed to start replay session");
            return new ReplayStartProtocolObject.StartResponse(false, e.getMessage());
        }
    }
}
