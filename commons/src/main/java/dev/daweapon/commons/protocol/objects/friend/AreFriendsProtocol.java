package dev.daweapon.commons.protocol.objects.friend;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for checking whether two players are friends.
public class AreFriendsProtocol extends RedisProtocol
        <AreFriendsProtocol.AreFriendsMessage,
                AreFriendsProtocol.AreFriendsResponse> {
    private static final Serializer<AreFriendsMessage> SERIALIZER =
            new JacksonSerializer<>(AreFriendsMessage.class);
    private static final Serializer<AreFriendsResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(AreFriendsResponse.class);

    @Override

    public Serializer<AreFriendsMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<AreFriendsResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record AreFriendsMessage(UUID player, UUID other) {
    }

    public record AreFriendsResponse(boolean areFriends, boolean success, @Nullable String error) {
    }
}
