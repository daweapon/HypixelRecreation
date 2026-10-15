package dev.daweapon.service.itemtracker.endpoints;

import dev.daweapon.commons.TrackedItem;
import dev.daweapon.commons.protocol.objects.itemtracker.TrackedItemRetrieveProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.itemtracker.TrackedItemsDatabase;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that fetches a tracked item record by UUID, throwing if it does not exist.
public class EndpointGetTrackedItem implements RedisMessageHandler<
        TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage,
        TrackedItemRetrieveProtocol.TrackedItemResponse> {

    @Override
    public TrackedItemRetrieveProtocol protocol() {
        return new TrackedItemRetrieveProtocol();
    }

    @Override
    public TrackedItemRetrieveProtocol.TrackedItemResponse handle(TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage messageObject, RedisMessageContext context) {
        UUID itemUUID = messageObject.itemUUID();

        if (!new TrackedItemsDatabase(itemUUID).exists()) {
            throw new RuntimeException("Attempted to get a tracked item that does not exist.");
        }

        TrackedItem item = new TrackedItemsDatabase(itemUUID).get();
        return new TrackedItemRetrieveProtocol.TrackedItemResponse(item, true, null);
    }
}