package dev.daweapon.service.friend.endpoints;

import dev.daweapon.commons.friend.PendingFriendRequest;
import dev.daweapon.commons.protocol.objects.friend.GetPendingFriendRequestsProtocol;
import dev.daweapon.service.friend.FriendCache;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that returns the pending friend requests addressed to a given player.
public class GetPendingRequestsEndpoint implements RedisMessageHandler<
        GetPendingFriendRequestsProtocol.GetPendingRequestsMessage,
        GetPendingFriendRequestsProtocol.GetPendingRequestsResponse> {

    @Override
    public GetPendingFriendRequestsProtocol protocol() {
        return new GetPendingFriendRequestsProtocol();
    }

    @Override
    public GetPendingFriendRequestsProtocol.GetPendingRequestsResponse handle(GetPendingFriendRequestsProtocol.GetPendingRequestsMessage messageObject, RedisMessageContext context) {

        List<PendingFriendRequest> requests = FriendCache.getPendingRequestsFor(messageObject.playerUuid());
        return new GetPendingFriendRequestsProtocol.GetPendingRequestsResponse(requests, true, null);
    }
}
