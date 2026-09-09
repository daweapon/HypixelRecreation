package net.swofty.type.skyblockgeneric.data.datapoints;

import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

public class DatapointAuctionOperations extends SkyBlockDatapoint<DatapointAuctionOperations.State> {
    public record State(long revision, String pending) {
        public State next(String request) { return new State(Math.incrementExact(revision), request); }
    }

    public DatapointAuctionOperations(String key) { this(key, new State(0, null)); }
    public DatapointAuctionOperations(String key, State value) {
        super(key, value, new Serializer<>() {
            @Override public String serialize(State state) {
                return new JSONObject().put("revision", state.revision()).put("pending", state.pending()).toString();
            }
            @Override public State deserialize(String json) {
                return read(json);
            }
            @Override public State clone(State state) { return state; }
        });
    }

    public static State read(String json) {
        if (json == null) return new State(0, null);
        var document = new JSONObject(json);
        return new State(document.getLong("revision"), document.optString("pending", null));
    }
}
