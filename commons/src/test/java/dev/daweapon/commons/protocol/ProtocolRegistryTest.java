package dev.daweapon.commons.protocol;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolRegistryTest {
    @Test
    void everyProtocolHasAUniqueChannel() throws Exception {
        Map<String, Integer> counts = new HashMap<>();
        for (RedisProtocol<?, ?> protocol : ProtocolSamples.protocols()) {
            counts.merge(protocol.channel(), 1, Integer::sum);
        }

        Set<String> duplicates = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        assertTrue(counts.size() >= 90, "expected the full protocol set but found " + counts.size());
        assertTrue(duplicates.isEmpty(), "duplicate protocol channels: " + duplicates);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void everyProtocolRoundTripsItsMessages() throws Exception {
        for (RedisProtocol protocol : ProtocolSamples.protocols()) {
            Type[] arguments = ProtocolSamples.messageTypes(protocol);
            assertNotNull(protocol.getSerializer(), protocol.channel());
            assertNotNull(protocol.getReturnSerializer(), protocol.channel());

            Object request = ProtocolSamples.sample(arguments[0]);
            if (request != null) {
                String json = protocol.translateToString(request);
                assertEquals(json, protocol.translateToString(protocol.translateFromString(json)), protocol.channel() + " request");
            }
            Object response = ProtocolSamples.sample(arguments[1]);
            if (response != null) {
                String json = protocol.translateReturnToString(response);
                assertEquals(json, protocol.translateReturnToString(protocol.translateReturnFromString(json)), protocol.channel() + " response");
            }
        }
    }
}
