package dev.daweapon;

import dev.daweapon.codec.Codec;

public interface DataField<T> {
    String namespace();
    String key();
    Codec<T> codec();
    T defaultValue();

    default String fullKey() {
        return namespace() + ":" + key();
    }
}
