package dev.daweapon.commons.protocol.objects.party;

import dev.daweapon.commons.party.PartyAction;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for submitting a PartyAction command to the party service.
public class SendPartyActionProtocol extends RedisProtocol<
        SendPartyActionProtocol.Request,
        SendPartyActionProtocol.Response> {
    private static final Serializer<Request> SERIALIZER =
            new JacksonSerializer<>(Request.class);
    private static final Serializer<Response> RETURN_SERIALIZER =
            new JacksonSerializer<>(Response.class);

    @Override
    public Serializer<Request> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Serializer<Response> getReturnSerializer() {
        return RETURN_SERIALIZER;
    }

    public record Request(PartyAction action) {}

    public record Response(boolean success, @Nullable String error) {
        public static Response ok() {
            return new Response(true, null);
        }

        public static Response failure(String error) {
            return new Response(false, error);
        }
    }
}
