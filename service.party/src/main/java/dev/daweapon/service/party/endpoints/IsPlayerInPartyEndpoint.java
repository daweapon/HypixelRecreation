package dev.daweapon.service.party.endpoints;

import dev.daweapon.commons.protocol.objects.party.IsPlayerInPartyProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.party.PartyCache;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that reports whether a given player is currently in a party.
public class IsPlayerInPartyEndpoint implements RedisMessageHandler<
        IsPlayerInPartyProtocol.IsPlayerInPartyMessage,
        IsPlayerInPartyProtocol.IsPlayerInPartyResponse> {

    @Override
    public IsPlayerInPartyProtocol protocol() {
        return new IsPlayerInPartyProtocol();
    }

    @Override
    public IsPlayerInPartyProtocol.IsPlayerInPartyResponse handle(IsPlayerInPartyProtocol.IsPlayerInPartyMessage messageObject, RedisMessageContext context) {
        UUID playerUUID = messageObject.playerUUID();

        boolean isInParty = PartyCache.isInParty(playerUUID);

        return new IsPlayerInPartyProtocol.IsPlayerInPartyResponse(isInParty, true, null);
    }
}