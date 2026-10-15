package dev.daweapon.commons.protocol.objects.election;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

public class ResolveElectionProtocol
        extends RedisProtocol<ResolveElectionProtocol.ResolveElectionMessage,
        ResolveElectionProtocol.ResolveElectionResponse> {
    private static final Serializer<ResolveElectionMessage> SERIALIZER =
            new JacksonSerializer<>(ResolveElectionMessage.class);
    private static final Serializer<ResolveElectionResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(ResolveElectionResponse.class);

    @Override
    public Serializer<ResolveElectionMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<ResolveElectionResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record ResolveElectionMessage(int year) {}

    public record ResolveElectionResponse(boolean resolved, String serializedData, boolean success, @Nullable String error) {}
}
