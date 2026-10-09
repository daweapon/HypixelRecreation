package net.swofty.commons.proxy;

import java.time.Duration;

public final class ProxyBackoff {
    public static final Duration INITIAL = Duration.ofSeconds(1);
    public static final Duration MAX = Duration.ofSeconds(30);

    private final Duration initial;
    private final Duration max;
    private Duration next;

    public ProxyBackoff() {
        this(INITIAL, MAX);
    }

    public ProxyBackoff(Duration initial, Duration max) {
        this.initial = initial;
        this.max = max;
        this.next = initial;
    }

    public synchronized Duration next() {
        Duration current = next;
        Duration doubled = current.multipliedBy(2);
        next = doubled.compareTo(max) > 0 ? max : doubled;
        return current;
    }

    public synchronized void reset() {
        next = initial;
    }
}
