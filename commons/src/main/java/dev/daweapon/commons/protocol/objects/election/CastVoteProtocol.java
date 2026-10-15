package dev.daweapon.commons.protocol.objects.election;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class CastVoteProtocol
        extends RedisProtocol<CastVoteProtocol.CastVoteMessage,
        CastVoteProtocol.CastVoteResponse> {
    private static final Serializer<CastVoteMessage> SERIALIZER =
            new JacksonSerializer<>(CastVoteMessage.class);
    private static final Serializer<CastVoteResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(CastVoteResponse.class);

    @Override
    public Serializer<CastVoteMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<CastVoteResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record CastVoteMessage(UUID accountId, String candidateName, long voteWeight) {}

    public record CastVoteResponse(boolean success, Map<String, Long> tallies, @Nullable String error) {}
}
