package dev.daweapon.service.friend.endpoints;

import dev.daweapon.commons.friend.FriendData;
import dev.daweapon.commons.protocol.objects.friend.GetFriendDataProtocol;
import dev.daweapon.service.friend.FriendCache;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that returns a player's full friend data.
public class GetFriendDataEndpoint implements RedisMessageHandler<
        GetFriendDataProtocol.GetFriendDataMessage,
        GetFriendDataProtocol.GetFriendDataResponse> {

    @Override
    public GetFriendDataProtocol protocol() {
        return new GetFriendDataProtocol();
    }

    @Override
    public GetFriendDataProtocol.GetFriendDataResponse handle(GetFriendDataProtocol.GetFriendDataMessage messageObject, RedisMessageContext context) {

        FriendData data = FriendCache.getFriendData(messageObject.playerUuid());
        return new GetFriendDataProtocol.GetFriendDataResponse(data, true, null);
    }
}
