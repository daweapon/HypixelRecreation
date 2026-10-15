package dev.daweapon.service.generic;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoReadinessTest {
    @Test
    void parsesHostWithDefaultPort() {
        List<InetSocketAddress> hosts = MongoReadiness.parseHosts("mongodb://mongodb");

        assertEquals(1, hosts.size());
        assertEquals("mongodb", hosts.getFirst().getHostString());
        assertEquals(27017, hosts.getFirst().getPort());
    }

    @Test
    void parsesCredentialsPortAndOptions() {
        List<InetSocketAddress> hosts = MongoReadiness.parseHosts("mongodb://user:pa:ss@db.local:27018/game?retryWrites=true");

        assertEquals("db.local", hosts.getFirst().getHostString());
        assertEquals(27018, hosts.getFirst().getPort());
    }

    @Test
    void parsesReplicaSetHosts() {
        assertEquals(2, MongoReadiness.parseHosts("mongodb://a:1,b:2/?replicaSet=rs").size());
    }

    @Test
    void srvAndMalformedUrisAreNotWaitedOn() {
        assertTrue(MongoReadiness.parseHosts("mongodb+srv://cluster.example.net").isEmpty());
        assertTrue(MongoReadiness.parseHosts("not-a-uri").isEmpty());
        assertTrue(MongoReadiness.parseHosts(null).isEmpty());
    }
}
