package dev.daweapon.commons.protocol.objects.friend;

import dev.daweapon.commons.friend.PendingFriendRequest;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for fetching a player's pending incoming friend requests.
public class GetPendingFriendRequestsProtocol extends RedisProtocol
        <GetPendingFriendRequestsProtocol.GetPendingRequestsMessage,
                GetPendingFriendRequestsProtocol.GetPendingRequestsResponse> {
    private static final Serializer<GetPendingRequestsMessage> SERIALIZER =
            new JacksonSerializer<>(GetPendingRequestsMessage.class);
    private static final Serializer<GetPendingRequestsResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(GetPendingRequestsResponse.class);

    @Override

    public Serializer<GetPendingRequestsMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<GetPendingRequestsResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record GetPendingRequestsMessage(UUID playerUuid) {
    }

    public record GetPendingRequestsResponse(List<PendingFriendRequest> requests, boolean success, @Nullable String error) {
    }
}
