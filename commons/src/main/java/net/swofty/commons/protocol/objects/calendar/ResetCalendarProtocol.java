package net.swofty.commons.protocol.objects.calendar;

import net.swofty.commons.protocol.JacksonSerializer;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;

// Broadcast from a staff /adminbypass resetyear invocation to every connected game server, so the
// SkyBlock Calendar resets everywhere at once instead of being overwritten back up moments later by
// SkyBlockServerAttributes' cross-server "highest elapsed wins" reconciliation.
public class ResetCalendarProtocol extends RedisProtocol<
        ResetCalendarProtocol.ResetMessage,
        ResetCalendarProtocol.ResetResponse> {

    private static final Serializer<ResetMessage> MESSAGE_SERIALIZER =
            new JacksonSerializer<>(ResetMessage.class);
    private static final Serializer<ResetResponse> RESPONSE_SERIALIZER =
            new JacksonSerializer<>(ResetResponse.class);

    @Override
    public Serializer<ResetMessage> getSerializer() {
        return MESSAGE_SERIALIZER;
    }

    @Override
    public Serializer<ResetResponse> getReturnSerializer() {
        return RESPONSE_SERIALIZER;
    }

    public record ResetMessage() {}
    public record ResetResponse(boolean success) {}
}
