package net.swofty.commons;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
// Simple mutable generic key/value pair.
public class Tuple<A, B> {
    private A key;
    private B value;

    public Tuple(A key, B value) {
        this.key = key;
        this.value = value;
    }

}
