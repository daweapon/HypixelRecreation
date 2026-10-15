package dev.daweapon.service.party.endpoints;

import dev.daweapon.commons.party.FullParty;
import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.party.GetPartyProtocol;
import dev.daweapon.commons.redis.RedisMessageHandler;
import dev.daweapon.service.party.PartyCache;

import java.util.UUID;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that looks up the party a given player belongs to, if any.
public class GetPartyEndpoint implements RedisMessageHandler
        <GetPartyProtocol.GetPartyMessage,
                GetPartyProtocol.GetPartyResponse> {
    @Override
    public RedisProtocol<GetPartyProtocol.GetPartyMessage, GetPartyProtocol.GetPartyResponse> protocol() {
        return new GetPartyProtocol();
    }

    @Override
    public GetPartyProtocol.GetPartyResponse handle(GetPartyProtocol.GetPartyMessage messageObject, RedisMessageContext context) {
        UUID memberUUID = messageObject.memberUuid();
        FullParty party = PartyCache.getPartyFromPlayer(memberUUID);
        // A solo player simply has no party — return an empty response instead of throwing,
        // otherwise the request never resolves and the caller times out (breaking game joins).
        if (party == null) return new GetPartyProtocol.GetPartyResponse(null, false, "Player is not in a party");
        return new GetPartyProtocol.GetPartyResponse(party, true, null);
    }
}
