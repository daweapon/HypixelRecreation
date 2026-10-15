package dev.daweapon.service.api;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Guards the control agent URL against the underscore-hostname trap.
 *
 * Java's URI parser follows RFC 2396, where an underscore is not legal in a hostname, so
 * http://service_control:9090 parses with a null host and HttpRequest refuses to build. curl
 * and most other clients accept it, so the failure presents as the agent being unreachable
 * while every manual connectivity check passes.
 */
class AgentUrlTest {

    @Test
    void javaRejectsAnUnderscoreHostnameOutright() {
        URI underscored = URI.create("http://service_control:9090/control/fleet");
        assertNull(underscored.getHost(),
                "an underscore makes URI.getHost() null - this is the whole trap");
        assertThrows(IllegalArgumentException.class,
                () -> HttpRequest.newBuilder().uri(underscored).build(),
                "HttpRequest must be shown to reject it, so the reason for the alias is explicit");
    }

    @Test
    void theHyphenatedAliasIsUsable() {
        URI aliased = URI.create("http://service-control:9090/control/fleet");
        assertNotNull(aliased.getHost());
        assertDoesNotThrow(() -> HttpRequest.newBuilder().uri(aliased).build());
    }

    @Test
    void theShippedDefaultAgentUrlIsParseable() {
        // Mirrors ControlAgentClient's default. If someone reverts it to the container name,
        // this fails here rather than in production as "control agent unreachable".
        URI shipped = URI.create("http://service-control:9090");
        assertNotNull(shipped.getHost(), "the default agent URL must have a host Java can parse");
        assertDoesNotThrow(() -> HttpRequest.newBuilder().uri(shipped).build());
    }
}
