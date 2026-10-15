package dev.daweapon.service.election.endpoints;

import com.google.gson.Gson;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.election.GetElectionDataProtocol;
import dev.daweapon.service.election.ElectionDatabase;
import dev.daweapon.commons.redis.RedisMessageHandler;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that returns the current election data (with votes stripped and live tallies attached) as JSON.
public class GetElectionDataEndpoint implements RedisMessageHandler
        <GetElectionDataProtocol.GetElectionDataMessage,
                GetElectionDataProtocol.GetElectionDataResponse> {

    private static final Gson GSON = new Gson();

    @Override
    public RedisProtocol<GetElectionDataProtocol.GetElectionDataMessage,
            GetElectionDataProtocol.GetElectionDataResponse> protocol() {
        return new GetElectionDataProtocol();
    }

    @Override
    @SuppressWarnings("unchecked")
    public GetElectionDataProtocol.GetElectionDataResponse handle(GetElectionDataProtocol.GetElectionDataMessage messageObject, RedisMessageContext context) {
        String data = ElectionDatabase.loadElectionData();
        if (data == null) {
            return new GetElectionDataProtocol.GetElectionDataResponse(false, null, true, null);
        }

        try {
            Map<String, Object> parsed = GSON.fromJson(data, Map.class);

            parsed.remove("votes");

            Number yearNum = (Number) parsed.get("electionYear");
            if (yearNum != null) {
                int electionYear = yearNum.intValue();
                Map<String, Long> tallies = ElectionDatabase.getTallies(electionYear);
                parsed.put("voteTallies", tallies);
            }

            return new GetElectionDataProtocol.GetElectionDataResponse(true, GSON.toJson(parsed), true, null);
        } catch (Exception e) {
            Logger.error(e, "Failed to parse election data");
            return new GetElectionDataProtocol.GetElectionDataResponse(false, null, true, null);
        }
    }
}
