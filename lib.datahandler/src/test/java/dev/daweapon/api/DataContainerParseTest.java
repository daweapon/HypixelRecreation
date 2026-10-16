package dev.daweapon.api;

import dev.daweapon.DataField;
import dev.daweapon.codec.Codec;
import dev.daweapon.codec.Codecs;
import dev.daweapon.data.DataFormat;
import dev.daweapon.data.DataReader;
import dev.daweapon.data.DataWriter;
import dev.daweapon.data.format.JsonFormat;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataContainerParseTest {
    private record IntField(String key) implements DataField<Integer> {
        public String namespace() { return "test"; }
        public Codec<Integer> codec() { return Codecs.INT; }
        public Integer defaultValue() { return 0; }
    }

    private static final class CountingFormat implements DataFormat {
        private final JsonFormat delegate = new JsonFormat();
        final AtomicInteger parses = new AtomicInteger();

        public DataReader createReader(byte[] data) { parses.incrementAndGet(); return delegate.createReader(data); }
        public DataWriter createWriter() { return delegate.createWriter(); }
        public byte[] toBytes(DataWriter writer) { return delegate.toBytes(writer); }
        public boolean randomAccessReaders() { return delegate.randomAccessReaders(); }
        public Map<String, Object> readRaw(byte[] data) { return delegate.readRaw(data); }
        public byte[] writeRaw(Map<String, Object> data) { return delegate.writeRaw(data); }
    }

    private static byte[] document(int fields) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < fields; i++) map.put("test:f" + i, i);
        return new JsonFormat().writeRaw(map);
    }

    @Test
    void readingManyFieldsParsesTheDocumentOnce() {
        CountingFormat format = new CountingFormat();
        DataContainer container = new DataContainer();
        container.loadDocument(format, document(200));
        for (int i = 0; i < 200; i++) {
            IntField field = new IntField("f" + i);
            container.ensureField(field, format);
            assertEquals(i, container.get(field));
        }
        assertEquals(1, format.parses.get());
    }

    @Test
    void aNewBackingDocumentIsParsedAgain() {
        CountingFormat format = new CountingFormat();
        DataContainer container = new DataContainer();
        container.loadDocument(format, document(3));
        container.ensureField(new IntField("f0"), format);
        container.reload(new JsonFormat().writeRaw(Map.of("test:f1", 41)));
        IntField changed = new IntField("f1");
        container.ensureField(changed, format);
        assertEquals(41, container.get(changed));
        assertEquals(2, format.parses.get());
    }

    @Test
    void aFormatWithoutRandomAccessIsParsedForEveryField() {
        JsonFormat json = new JsonFormat();
        AtomicInteger parses = new AtomicInteger();
        DataFormat sequential = new DataFormat() {
            public DataReader createReader(byte[] data) { parses.incrementAndGet(); return json.createReader(data); }
            public DataWriter createWriter() { return json.createWriter(); }
            public byte[] toBytes(DataWriter writer) { return json.toBytes(writer); }
        };
        DataContainer container = new DataContainer();
        container.loadDocument(sequential, document(5));
        for (int i = 0; i < 5; i++) container.ensureField(new IntField("f" + i), sequential);
        assertEquals(5, parses.get());
    }

}
