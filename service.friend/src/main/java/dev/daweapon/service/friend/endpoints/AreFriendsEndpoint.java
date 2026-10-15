package dev.daweapon.service.friend.endpoints;

import dev.daweapon.commons.protocol.objects.friend.AreFriendsProtocol;
import dev.daweapon.service.friend.FriendCache;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that reports whether two players are friends with each other.
public class AreFriendsEndpoint implements RedisMessageHandler<
        AreFriendsProtocol.AreFriendsMessage,
        AreFriendsProtocol.AreFriendsResponse> {

    @Override
    public AreFriendsProtocol protocol() {
        return new AreFriendsProtocol();
    }

    @Override
    public AreFriendsProtocol.AreFriendsResponse handle(AreFriendsProtocol.AreFriendsMessage messageObject, RedisMessageContext context) {

        boolean areFriends = FriendCache.areFriends(messageObject.player(), messageObject.other());
        return new AreFriendsProtocol.AreFriendsResponse(areFriends, true, null);
    }
}
