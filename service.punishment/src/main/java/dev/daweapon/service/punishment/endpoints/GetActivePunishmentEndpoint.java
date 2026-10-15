package dev.daweapon.service.punishment.endpoints;

import dev.daweapon.commons.protocol.RedisProtocol;
import dev.daweapon.commons.protocol.objects.punishment.GetActivePunishmentProtocol;
import dev.daweapon.commons.punishment.ActivePunishment;
import dev.daweapon.commons.punishment.PunishmentRedis;
import dev.daweapon.commons.redis.RedisMessageHandler;

import java.util.List;
import java.util.Optional;
import dev.daweapon.commons.redis.RedisMessageContext;

// Redis endpoint that looks up a player's currently active punishment of a given type, if any.
public class GetActivePunishmentEndpoint implements RedisMessageHandler
        <GetActivePunishmentProtocol.GetActivePunishmentMessage,
                GetActivePunishmentProtocol.GetActivePunishmentResponse> {

    @Override
    public RedisProtocol<GetActivePunishmentProtocol.GetActivePunishmentMessage, GetActivePunishmentProtocol.GetActivePunishmentResponse> protocol() {
        return new GetActivePunishmentProtocol();
    }

    @Override
    public GetActivePunishmentProtocol.GetActivePunishmentResponse handle(GetActivePunishmentProtocol.GetActivePunishmentMessage messageObject, RedisMessageContext context) {
        Optional<ActivePunishment> existing = PunishmentRedis.getActive(messageObject.target(), messageObject.type());
        if (existing.isEmpty()) {
            return new GetActivePunishmentProtocol.GetActivePunishmentResponse(false, null, null, null, 0, List.of(), true, null);
        }

        ActivePunishment punishment = existing.get();
        return new GetActivePunishmentProtocol.GetActivePunishmentResponse(
                true,
                punishment.type(),
                punishment.banId(),
                punishment.reason(),
                punishment.expiresAt(),
                punishment.tags(),
                true,
                null
        );
    }
}
