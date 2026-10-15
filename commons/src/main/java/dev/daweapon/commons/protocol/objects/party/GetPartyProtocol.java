package dev.daweapon.commons.protocol.objects.party;

import dev.daweapon.commons.party.Party;
import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for fetching the party a given member belongs to.
public class GetPartyProtocol extends RedisProtocol
        <GetPartyProtocol.GetPartyMessage,
                GetPartyProtocol.GetPartyResponse> {
    private static final Serializer<GetPartyMessage> SERIALIZER =
            new JacksonSerializer<>(GetPartyMessage.class);
    private static final Serializer<GetPartyResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(GetPartyResponse.class);

    @Override

    public Serializer<GetPartyMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<GetPartyResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record GetPartyMessage(UUID memberUuid) { }

    public record GetPartyResponse(Party party, boolean success, @Nullable String error) { }
}
