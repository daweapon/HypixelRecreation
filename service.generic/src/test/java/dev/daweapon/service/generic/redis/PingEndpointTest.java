package dev.daweapon.service.generic.redis;

import dev.daweapon.commons.protocol.objects.PingProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PingEndpointTest {
    private final PingEndpoint endpoint = new PingEndpoint();

    @Test
    void answersWithEmptyMessage() {
        assertEquals(new PingProtocol.EmptyMessage(), endpoint.handle(new PingProtocol.EmptyMessage(), null));
    }

    @Test
    void usesPingProtocolOnPingChannel() {
        assertInstanceOf(PingProtocol.class, endpoint.protocol());
        assertEquals("PingProtocol", endpoint.protocol().channel());
    }
}
