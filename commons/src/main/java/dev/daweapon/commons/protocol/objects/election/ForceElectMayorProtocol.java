package dev.daweapon.commons.protocol.objects.election;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Admin-only: unconditionally overwrites the stored election data with the given serialized
// state, regardless of whether an election is currently open. Used by /adminbypass to force-elect
// a mayor for testing without going through a real vote/resolve cycle.
public class ForceElectMayorProtocol
        extends RedisProtocol<ForceElectMayorProtocol.ForceElectMayorMessage,
        ForceElectMayorProtocol.ForceElectMayorResponse> {
    private static final Serializer<ForceElectMayorMessage> SERIALIZER =
            new JacksonSerializer<>(ForceElectMayorMessage.class);
    private static final Serializer<ForceElectMayorResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(ForceElectMayorResponse.class);

    @Override
    public Serializer<ForceElectMayorMessage> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<ForceElectMayorResponse> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record ForceElectMayorMessage(String serializedData) {}

    public record ForceElectMayorResponse(boolean success, @Nullable String serializedData, @Nullable String error) {}
}
