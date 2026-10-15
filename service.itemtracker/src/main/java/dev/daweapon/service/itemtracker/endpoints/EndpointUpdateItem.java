package dev.daweapon.service.itemtracker.endpoints;

import dev.daweapon.commons.TrackedItem;
import dev.daweapon.commons.protocol.objects.itemtracker.TrackedItemUpdateProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.itemtracker.TrackedItemsDatabase;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that creates a tracked item record if missing, or updates its attached-player history if it already exists.
public class EndpointUpdateItem implements RedisMessageHandler<
        TrackedItemUpdateProtocol.TrackedItemUpdateMessage,
        TrackedItemUpdateProtocol.TrackedItemUpdateResponse> {

    @Override
    public TrackedItemUpdateProtocol protocol() {
        return new TrackedItemUpdateProtocol();
    }

    @Override
    public TrackedItemUpdateProtocol.TrackedItemUpdateResponse handle(TrackedItemUpdateProtocol.TrackedItemUpdateMessage messageObject, RedisMessageContext context) {
        UUID itemUUID = messageObject.itemUUID();
        UUID attachedPlayerUUID = messageObject.attachedPlayerUUID();
        UUID attachedPlayerProfile = messageObject.attachedPlayerProfile();
        String itemType = messageObject.itemType();

        Thread.startVirtualThread(() -> {
            if (!new TrackedItemsDatabase(itemUUID).exists()) {
                TrackedItem item = TrackedItem.newTrackedItem(
                        itemUUID,
                        attachedPlayerUUID,
                        attachedPlayerProfile,
                        itemType,
                        TrackedItemsDatabase.getNumberMade(itemType)
                );
                new TrackedItemsDatabase(itemUUID).insertOrUpdate(item);
            } else {
                TrackedItem item = new TrackedItemsDatabase(itemUUID).get();
                item.addOrUpdateAttachedPlayer(attachedPlayerUUID, attachedPlayerProfile);
                new TrackedItemsDatabase(itemUUID).insertOrUpdate(item);
            }
        });

        return new TrackedItemUpdateProtocol.TrackedItemUpdateResponse();
    }
}
