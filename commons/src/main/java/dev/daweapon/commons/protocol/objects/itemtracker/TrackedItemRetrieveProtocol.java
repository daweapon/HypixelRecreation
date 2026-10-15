package dev.daweapon.commons.protocol.objects.itemtracker;

import dev.daweapon.commons.TrackedItem;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class TrackedItemRetrieveProtocol extends RedisProtocol
        <TrackedItemRetrieveProtocol.TrackedItemRetrieveMessage,
        TrackedItemRetrieveProtocol.TrackedItemResponse> {
    private static final Serializer<TrackedItemRetrieveMessage> SERIALIZER =
            new JacksonSerializer<>(TrackedItemRetrieveMessage.class);
    private static final Serializer<TrackedItemResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(TrackedItemResponse.class);

    @Override

    public Serializer<TrackedItemRetrieveMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<TrackedItemResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record TrackedItemRetrieveMessage(UUID itemUUID) {
        public TrackedItemRetrieveMessage(String itemUUID) {
            this(UUID.fromString(itemUUID));
        }
    }

    public record TrackedItemResponse(TrackedItem trackedItem, boolean success, @Nullable String error) { }
}
