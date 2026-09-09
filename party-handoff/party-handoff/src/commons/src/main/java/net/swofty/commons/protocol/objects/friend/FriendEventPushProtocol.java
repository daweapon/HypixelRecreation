package net.swofty.commons.protocol.objects.friend;

import net.swofty.commons.protocol.RedisProtocol;

import java.util.List;
import java.util.UUID;

// Redis RPC protocol for the friend service to push a serialized FriendEvent out to a server for delivery to its players.
public class FriendEventPushProtocol
        extends RedisProtocol<FriendEventPushProtocol.Request, FriendEventPushProtocol.Response> {

    public FriendEventPushProtocol() {
        super(Request.class, Response.class);
    }

    public record Request(String eventType, String eventData, List<UUID> participants) {}

    public record Response(boolean success, int playersHandled, List<UUID> playersHandledUUIDs, String error) {
        public static Response failure(String reason) {
            return new Response(false, 0, List.of(), reason);
        }

        public static Response success(int playersHandled, List<UUID> playersHandledUUIDs) {
            return new Response(true, playersHandled, playersHandledUUIDs, null);
        }
    }
}
