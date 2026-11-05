import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Nbt {
    static final int END = 0, BYTE = 1, SHORT = 2, INT = 3, LONG = 4, FLOAT = 5, DOUBLE = 6, BYTE_ARRAY = 7,
            STRING = 8, LIST = 9, COMPOUND = 10, INT_ARRAY = 11, LONG_ARRAY = 12;

    record Tag(int type, Object value) {
        @SuppressWarnings("unchecked")
        Map<String, Tag> compound() {
            return (Map<String, Tag>) value;
        }

        @SuppressWarnings("unchecked")
        List<Tag> list() {
            return (List<Tag>) value;
        }

        Tag get(String key) {
            return compound().get(key);
        }

        String string() {
            return (String) value;
        }

        int intValue() {
            return ((Number) value).intValue();
        }

        long[] longs() {
            return (long[]) value;
        }
    }

    record ListValue(int elementType, List<Tag> items) {
    }

    private Nbt() {
    }

    static Tag read(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        int type = data.readUnsignedByte();
        if (type != COMPOUND) throw new IOException("Root is not a compound: " + type);
        data.readUTF();
        return payload(data, type);
    }

    static Tag read(byte[] bytes) throws IOException {
        return read(new ByteArrayInputStream(bytes));
    }

    private static Tag payload(DataInputStream in, int type) throws IOException {
        return switch (type) {
            case BYTE -> new Tag(type, in.readByte());
            case SHORT -> new Tag(type, in.readShort());
            case INT -> new Tag(type, in.readInt());
            case LONG -> new Tag(type, in.readLong());
            case FLOAT -> new Tag(type, in.readFloat());
            case DOUBLE -> new Tag(type, in.readDouble());
            case BYTE_ARRAY -> {
                byte[] bytes = new byte[in.readInt()];
                in.readFully(bytes);
                yield new Tag(type, bytes);
            }
            case STRING -> new Tag(type, readString(in));
            case LIST -> {
                int elementType = in.readUnsignedByte();
                int size = in.readInt();
                List<Tag> items = new ArrayList<>(Math.max(size, 0));
                for (int i = 0; i < size; i++) items.add(payload(in, elementType));
                yield new Tag(type, items);
            }
            case COMPOUND -> {
                Map<String, Tag> map = new LinkedHashMap<>();
                while (true) {
                    int child = in.readUnsignedByte();
                    if (child == END) break;
                    String name = readString(in);
                    map.put(name, payload(in, child));
                }
                yield new Tag(type, map);
            }
            case INT_ARRAY -> {
                int[] ints = new int[in.readInt()];
                for (int i = 0; i < ints.length; i++) ints[i] = in.readInt();
                yield new Tag(type, ints);
            }
            case LONG_ARRAY -> {
                long[] longs = new long[in.readInt()];
                for (int i = 0; i < longs.length; i++) longs[i] = in.readLong();
                yield new Tag(type, longs);
            }
            default -> throw new IOException("Unknown tag type " + type);
        };
    }

    private static String readString(DataInputStream in) throws IOException {
        byte[] bytes = new byte[in.readUnsignedShort()];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static void write(OutputStream out, String rootName, Tag root) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeByte(COMPOUND);
        writeString(data, rootName);
        writePayload(data, root);
        data.flush();
    }

    static byte[] bytes(String rootName, Tag root) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, rootName, root);
        return out.toByteArray();
    }

    private static void writePayload(DataOutputStream out, Tag tag) throws IOException {
        switch (tag.type()) {
            case BYTE -> out.writeByte((Byte) tag.value());
            case SHORT -> out.writeShort((Short) tag.value());
            case INT -> out.writeInt((Integer) tag.value());
            case LONG -> out.writeLong((Long) tag.value());
            case FLOAT -> out.writeFloat((Float) tag.value());
            case DOUBLE -> out.writeDouble((Double) tag.value());
            case BYTE_ARRAY -> {
                byte[] bytes = (byte[]) tag.value();
                out.writeInt(bytes.length);
                out.write(bytes);
            }
            case STRING -> writeString(out, tag.string());
            case LIST -> {
                List<Tag> items = tag.list();
                out.writeByte(items.isEmpty() ? END : items.getFirst().type());
                out.writeInt(items.size());
                for (Tag item : items) writePayload(out, item);
            }
            case COMPOUND -> {
                for (Map.Entry<String, Tag> entry : tag.compound().entrySet()) {
                    out.writeByte(entry.getValue().type());
                    writeString(out, entry.getKey());
                    writePayload(out, entry.getValue());
                }
                out.writeByte(END);
            }
            case INT_ARRAY -> {
                int[] ints = (int[]) tag.value();
                out.writeInt(ints.length);
                for (int value : ints) out.writeInt(value);
            }
            case LONG_ARRAY -> {
                long[] longs = (long[]) tag.value();
                out.writeInt(longs.length);
                for (long value : longs) out.writeLong(value);
            }
            default -> throw new IOException("Unknown tag type " + tag.type());
        }
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    static Tag compound(Map<String, Tag> map) {
        return new Tag(COMPOUND, map);
    }

    static Tag string(String value) {
        return new Tag(STRING, value);
    }

    static Tag intTag(int value) {
        return new Tag(INT, value);
    }

    static Tag shortTag(int value) {
        return new Tag(SHORT, (short) value);
    }

    static Tag intArray(int... values) {
        return new Tag(INT_ARRAY, values);
    }

    static Tag byteArray(byte[] values) {
        return new Tag(BYTE_ARRAY, values);
    }

    static Tag list(List<Tag> items) {
        return new Tag(LIST, items);
    }
}
