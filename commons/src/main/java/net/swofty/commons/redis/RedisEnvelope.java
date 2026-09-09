package net.swofty.commons.redis;

import org.json.JSONObject;

// Wire envelope wrapping a Redis RPC message's correlation id, sender, and serialized payload.
public record RedisEnvelope(String id, String from, String payload) {
    public String serialize() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("from", from);
        json.put("payload", payload);
        return json.toString();
    }

    public static RedisEnvelope deserialize(String json) {
        JSONObject obj = new JSONObject(json);
        return new RedisEnvelope(
                obj.getString("id"),
                obj.getString("from"),
                obj.optString("payload", "")
        );
    }
}
