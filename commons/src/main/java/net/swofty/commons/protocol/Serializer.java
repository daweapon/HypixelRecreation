package net.swofty.commons.protocol;

// Contract for converting a value to/from its string wire representation, plus a deep-copy helper.
public interface Serializer<T> {
    String serialize(T value);

    T deserialize(String json);

    T clone(T value);
}