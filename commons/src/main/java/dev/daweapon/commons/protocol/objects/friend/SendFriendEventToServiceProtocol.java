package dev.daweapon.commons.protocol.objects.friend;

import dev.daweapon.commons.friend.FriendEvent;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for submitting a FriendEvent request to the friend service for processing.
public class SendFriendEventToServiceProtocol extends RedisProtocol
        <SendFriendEventToServiceProtocol.SendFriendEventToServiceMessage,
                SendFriendEventToServiceProtocol.SendFriendEventToServiceResponse> {
    private static final Serializer<SendFriendEventToServiceMessage> SERIALIZER =
            new JacksonSerializer<>(SendFriendEventToServiceMessage.class);
    private static final Serializer<SendFriendEventToServiceResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(SendFriendEventToServiceResponse.class);

    @Override

    public Serializer<SendFriendEventToServiceMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<SendFriendEventToServiceResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record SendFriendEventToServiceMessage(FriendEvent event) {
    }

    public record SendFriendEventToServiceResponse(boolean success, @Nullable String error) {
    }
}
