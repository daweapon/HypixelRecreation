package dev.daweapon.service.friend.endpoints;

import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.objects.presence.UpdatePresenceProtocol;
import dev.daweapon.service.friend.FriendCache;
import dev.daweapon.service.friend.PresenceStorage;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that updates a player's presence and, on an online/offline transition, notifies their friends.
public class UpdatePresenceEndpoint implements RedisMessageHandler<
        UpdatePresenceProtocol.UpdatePresenceMessage,
        UpdatePresenceProtocol.UpdatePresenceResponse> {

    @Override
    public UpdatePresenceProtocol protocol() {
        return new UpdatePresenceProtocol();
    }

    @Override
    public UpdatePresenceProtocol.UpdatePresenceResponse handle(UpdatePresenceProtocol.UpdatePresenceMessage messageObject, RedisMessageContext context) {

        PresenceInfo incoming = messageObject.presence();
        PresenceInfo previous = PresenceStorage.upsertPreservingServer(incoming);

        boolean stateChanged = previous == null || previous.isOnline() != incoming.isOnline();

        if (stateChanged) {
            String playerName = FriendCache.getPlayerName(incoming.getUuid());
            if (incoming.isOnline()) {
                FriendCache.handlePlayerJoin(incoming.getUuid(), playerName);
            } else {
                FriendCache.handlePlayerLeave(incoming.getUuid(), playerName);
            }
        }

        return new UpdatePresenceProtocol.UpdatePresenceResponse(true, null);
    }
}

