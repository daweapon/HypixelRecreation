package dev.daweapon.service.election.endpoints;

import com.google.gson.Gson;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.election.GetPlayerVoteProtocol;
import dev.daweapon.service.election.ElectionDatabase;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.Map;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that returns which candidate a given player voted for in the current election year, if any.
public class GetPlayerVoteEndpoint implements RedisMessageHandler
        <GetPlayerVoteProtocol.GetPlayerVoteMessage,
                GetPlayerVoteProtocol.GetPlayerVoteResponse> {

    private static final Gson GSON = new Gson();

    @Override
    public RedisProtocol<GetPlayerVoteProtocol.GetPlayerVoteMessage,
            GetPlayerVoteProtocol.GetPlayerVoteResponse> protocol() {
        return new GetPlayerVoteProtocol();
    }

    @Override
    @SuppressWarnings("unchecked")
    public GetPlayerVoteProtocol.GetPlayerVoteResponse handle(GetPlayerVoteProtocol.GetPlayerVoteMessage messageObject, RedisMessageContext context) {
        try {
            String rawData = ElectionDatabase.loadElectionData();
            if (rawData == null) {
                return new GetPlayerVoteProtocol.GetPlayerVoteResponse(null, true, null);
            }

            Map<String, Object> data = GSON.fromJson(rawData, Map.class);
            Number yearNum = (Number) data.get("electionYear");
            if (yearNum == null) {
                return new GetPlayerVoteProtocol.GetPlayerVoteResponse(null, true, null);
            }

            String vote = ElectionDatabase.getPlayerVote(
                    messageObject.accountId().toString(),
                    yearNum.intValue()
            );
            return new GetPlayerVoteProtocol.GetPlayerVoteResponse(vote, true, null);
        } catch (Exception e) {
            Logger.error(e, "Failed to get player vote");
            return new GetPlayerVoteProtocol.GetPlayerVoteResponse(null, true, null);
        }
    }
}
