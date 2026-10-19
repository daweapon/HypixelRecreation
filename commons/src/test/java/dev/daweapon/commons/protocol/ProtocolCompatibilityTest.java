package dev.daweapon.commons.protocol;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocolCompatibilityTest {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void everyProtocolToleratesFieldsAddedByANewerBuild() throws Exception {
        List<String> failures = new ArrayList<>();
        int checked = 0;
        for (RedisProtocol protocol : ProtocolSamples.protocols()) {
            Type[] arguments = ProtocolSamples.messageTypes(protocol);
            Object request = ProtocolSamples.sample(arguments[0]);
            if (request != null) {
                checked += checkRequest(protocol, request, failures);
            }
            Object response = ProtocolSamples.sample(arguments[1]);
            if (response != null) {
                checked += checkResponse(protocol, response, failures);
            }
        }
        assertTrue(checked >= 120, "expected to check most of the protocol messages but checked " + checked);
        assertEquals(List.of(), failures, "messages that break when a newer build adds a field");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int checkRequest(RedisProtocol protocol, Object sample, List<String> failures) throws Exception {
        String json = withExtraField(protocol.translateToString(sample));
        if (json == null) return 0;
        try {
            assertEquals(protocol.translateToString(sample), protocol.translateToString(protocol.translateFromString(json)));
        } catch (RuntimeException | AssertionError e) {
            failures.add(protocol.channel() + " request: " + e.getMessage());
        }
        return 1;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int checkResponse(RedisProtocol protocol, Object sample, List<String> failures) throws Exception {
        String json = withExtraField(protocol.translateReturnToString(sample));
        if (json == null) return 0;
        try {
            assertEquals(protocol.translateReturnToString(sample), protocol.translateReturnToString(protocol.translateReturnFromString(json)));
        } catch (RuntimeException | AssertionError e) {
            failures.add(protocol.channel() + " response: " + e.getMessage());
        }
        return 1;
    }

    private static String withExtraField(String json) throws Exception {
        JsonNode node = MAPPER.readTree(json);
        if (!(node instanceof ObjectNode object)) return null;
        object.put("addedByANewerBuild", 1);
        return object.toString();
    }
}
