package dev.daweapon.redisapi.api.requests;

import dev.daweapon.redisapi.api.ChannelRegistry;
import dev.daweapon.redisapi.api.RedisAPI;
import dev.daweapon.redisapi.events.RedisMessagingReceiveInterface;
import dev.daweapon.redisapi.util.RedisParsableMessage;
import org.jetbrains.annotations.ApiStatus;
import org.json.JSONObject;

/**
 * Internal use, not for outside API
 */
@ApiStatus.Internal
public class DataStreamListener implements RedisMessagingReceiveInterface {
    @Override
    public void onMessage(String channel, String message) {
        RedisParsableMessage msg = RedisParsableMessage.parse(message);
        DataRequest.StreamType type = DataRequest.StreamType.valueOf(msg.get("stream", "NONE"));
        String key = msg.get("key", "NONE");
        String id = msg.get("id", "NONE");
        String sender = msg.get("sender", "NONE");
        JSONObject data = msg.getJson().getJSONObject("data");

        switch (type) {
            case REQUEST -> {
                DataRequestResponder responder = DataRequestResponder.get(key);
                if (responder == null) return;

                JSONObject response = responder.respond(data);
                JSONObject responseJson = new JSONObject();
                responseJson.put("id", id);
                responseJson.put("sender", RedisAPI.getInstance().getFilterId());
                responseJson.put("stream", DataRequest.StreamType.RESPONSE.name());
                responseJson.put("key", key);
                responseJson.put("data", response);

                RedisAPI.getInstance().publishMessage(sender, ChannelRegistry.getFromName("internal-data-request"),
                        RedisParsableMessage.build(responseJson).formatForSend());
            }
            case RESPONSE -> {
                DataRequest.receive(id, data);
            }
        }
    }
}
