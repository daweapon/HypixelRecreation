package dev.daweapon.commons.redis;

import dev.daweapon.commons.protocol.objects.guild.GuildEventPushProtocol;
import dev.daweapon.commons.protocol.objects.party.SendPartyActionProtocol;
import dev.daweapon.redisapi.api.RedisAPI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisClientProtocolTest {
    @BeforeAll
    static void initializeRedis() {
        RedisAPI.generateInstance("redis://127.0.0.1:6399");
    }

    @Test
    void routesRequestsWithTheSameSimpleNameInEitherRegistrationOrder() {
        var party = new SendPartyActionProtocol();
        var guild = new GuildEventPushProtocol();

        RedisClient.registerResponseProtocols(List.of(party, guild));
        assertSame(party, RedisClient.protocolFor(new SendPartyActionProtocol.Request(null)));
        assertSame(guild, RedisClient.protocolFor(new GuildEventPushProtocol.Request("", "", List.of())));

        RedisClient.registerResponseProtocols(List.of(guild, party));
        assertSame(party, RedisClient.protocolFor(new SendPartyActionProtocol.Request(null)));
        assertSame(guild, RedisClient.protocolFor(new GuildEventPushProtocol.Request("", "", List.of())));
    }

    @Test
    void rejectsUnregisteredRequestEvenWhenItsSimpleNameMatches() {
        RedisClient.registerResponseProtocol(new SendPartyActionProtocol());
        assertThrows(IllegalArgumentException.class, () -> RedisClient.protocolFor(new Request()));
    }

    private record Request() {}
}
