package dev.daweapon.anticheat.flag;

import lombok.Getter;
import lombok.Setter;
import dev.daweapon.anticheat.event.AntiCheatListener;

@Getter
@Setter
// Base class for a single detection check, carrying the certainty and timestamp of one flag instance.
public abstract class Flag extends AntiCheatListener {
    private final long timestamp;

    private double certainty = 0;

    public Flag() {
        this.timestamp = System.currentTimeMillis();
    }
}
