package dev.daweapon.service.friend.endpoints;

import dev.daweapon.commons.presence.PresenceInfo;
import dev.daweapon.commons.protocol.objects.presence.GetPresenceBulkProtocol;
import dev.daweapon.service.friend.PresenceStorage;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that bulk-fetches presence info for a list of player UUIDs.
public class GetPresenceEndpoint implements RedisMessageHandler<
        GetPresenceBulkProtocol.GetPresenceBulkMessage,
        GetPresenceBulkProtocol.GetPresenceBulkResponse> {

    @Override
    public GetPresenceBulkProtocol protocol() {
        return new GetPresenceBulkProtocol();
    }

    @Override
    public GetPresenceBulkProtocol.GetPresenceBulkResponse handle(GetPresenceBulkProtocol.GetPresenceBulkMessage messageObject, RedisMessageContext context) {

        List<PresenceInfo> presence = PresenceStorage.getBulk(messageObject.uuids());
        return new GetPresenceBulkProtocol.GetPresenceBulkResponse(presence, true, null);
    }
}

