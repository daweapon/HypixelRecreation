package dev.daweapon.commons.protocol.objects.party;

import dev.daweapon.commons.protocol.JacksonSerializer;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.Serializer;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

// Redis RPC protocol for checking whether a player is currently in a party.
public class IsPlayerInPartyProtocol extends RedisProtocol
        <IsPlayerInPartyProtocol.IsPlayerInPartyMessage,
                IsPlayerInPartyProtocol.IsPlayerInPartyResponse> {
    private static final Serializer<IsPlayerInPartyMessage> SERIALIZER =
            new JacksonSerializer<>(IsPlayerInPartyMessage.class);
    private static final Serializer<IsPlayerInPartyResponse> RETURN_SERIALIZER =
            new JacksonSerializer<>(IsPlayerInPartyResponse.class);

    @Override

    public Serializer<IsPlayerInPartyMessage> getSerializer() {

        return SERIALIZER;

    }

    @Override

    public Serializer<IsPlayerInPartyResponse> getReturnSerializer() {

        return RETURN_SERIALIZER;

    }

    public record IsPlayerInPartyMessage(UUID playerUUID) {
    }

    public record IsPlayerInPartyResponse(boolean isInParty, boolean success, @Nullable String error) {
    }
}
