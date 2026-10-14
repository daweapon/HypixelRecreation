package net.swofty.commons.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonSerializerCompatibilityTest {
    record Message(String name, int amount) {}

    @Test
    void ignoresFieldsAddedByNewerSenders() {
        Message message = new JacksonSerializer<>(Message.class)
                .deserialize("{\"name\":\"a\",\"amount\":2,\"addedLater\":true}");

        assertEquals(new Message("a", 2), message);
    }

    @Test
    void fillsFieldsMissingFromOlderSenders() {
        Message message = new JacksonSerializer<>(Message.class).deserialize("{\"name\":\"a\"}");

        assertEquals(new Message("a", 0), message);
    }
}
