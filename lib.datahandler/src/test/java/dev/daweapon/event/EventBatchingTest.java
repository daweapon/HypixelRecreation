package dev.daweapon.event;

import dev.daweapon.DataField;
import dev.daweapon.codec.Codec;
import dev.daweapon.codec.Codecs;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventBatchingTest {
    private record IntField(String key) implements DataField<Integer> {
        public String namespace() { return "test"; }
        public Codec<Integer> codec() { return Codecs.INT; }
        public Integer defaultValue() { return 0; }
    }

    private static final class RecordingHandler implements PubSubHandler {
        final List<String> single = new ArrayList<>();
        final List<List<String>> batches = new ArrayList<>();

        public void publish(String message) { single.add(message); }
        public void publishAll(List<String> messages) { batches.add(List.copyOf(messages)); }
        public void subscribe(MessageHandler handler) {}
        public void shutdown() {}
    }

    @Test
    void eventsOutsideABatchArePublishedImmediately() {
        RecordingHandler handler = new RecordingHandler();
        DistributedEventBus bus = new DistributedEventBus(handler);
        bus.firePlayerDataChanged(new IntField("a"), UUID.randomUUID(), 1, 2, 1L);
        assertEquals(1, handler.single.size());
        assertEquals(0, handler.batches.size());
    }

    @Test
    void eventsInsideABatchAreFlushedTogetherInOrder() {
        RecordingHandler handler = new RecordingHandler();
        DistributedEventBus bus = new DistributedEventBus(handler);
        UUID player = UUID.randomUUID();
        bus.batch(() -> {
            for (int i = 0; i < 5; i++) bus.firePlayerDataChanged(new IntField("f" + i), player, i, i + 1, 7L);
            assertEquals(0, handler.single.size() + handler.batches.size());
        });
        assertEquals(0, handler.single.size());
        assertEquals(1, handler.batches.size());
        List<String> sent = handler.batches.get(0);
        assertEquals(5, sent.size());
        for (int i = 0; i < 5; i++) assertEquals(true, sent.get(i).contains("test:f" + i));
    }

    @Test
    void nestedBatchesFlushOnceAtTheOutermostEnd() {
        RecordingHandler handler = new RecordingHandler();
        DistributedEventBus bus = new DistributedEventBus(handler);
        UUID player = UUID.randomUUID();
        bus.batch(() -> {
            bus.firePlayerDataChanged(new IntField("a"), player, 1, 2, 1L);
            bus.batch(() -> bus.firePlayerDataChanged(new IntField("b"), player, 1, 2, 1L));
            assertEquals(0, handler.batches.size());
        });
        assertEquals(1, handler.batches.size());
        assertEquals(2, handler.batches.get(0).size());
    }

    @Test
    void aFailingActionStillFlushesWhatWasAlreadyFired() {
        RecordingHandler handler = new RecordingHandler();
        DistributedEventBus bus = new DistributedEventBus(handler);
        UUID player = UUID.randomUUID();
        assertThrows(IllegalStateException.class, () -> bus.batch(() -> {
            bus.firePlayerDataChanged(new IntField("a"), player, 1, 2, 1L);
            throw new IllegalStateException("boom");
        }));
        assertEquals(1, handler.batches.size());
        bus.firePlayerDataChanged(new IntField("b"), player, 1, 2, 2L);
        assertEquals(1, handler.single.size());
    }

    @Test
    void theDefaultPublishAllSendsEachMessage() {
        List<String> sent = new ArrayList<>();
        PubSubHandler handler = new PubSubHandler() {
            public void publish(String message) { sent.add(message); }
            public void subscribe(MessageHandler h) {}
            public void shutdown() {}
        };
        handler.publishAll(List.of("a", "b", "c"));
        assertEquals(List.of("a", "b", "c"), sent);
    }
}
