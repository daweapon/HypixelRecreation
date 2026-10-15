package dev.daweapon.commons.protocol.objects.friend;

import dev.daweapon.commons.friend.FriendData;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for fetching a player's full FriendData document.
public class GetFriendDataProtocol extends RedisProtocol
        <GetFriendDataProtocol.GetFriendDataMessage,
                GetFriendDataProtocol.GetFriendDataResponse> {
    private static final Serializer<GetFriendDataMessage> SERIALIZER =
            new JacksonSerializer<>(GetFriendDataMessage.class);
    private static final Serializer<GetFriendDataResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(GetFriendDataResponse.class);

    @Override

    public Serializer<GetFriendDataMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<GetFriendDataResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record GetFriendDataMessage(UUID playerUuid) {
    }

    public record GetFriendDataResponse(FriendData friendData, boolean success, @Nullable String error) {
    }
}
