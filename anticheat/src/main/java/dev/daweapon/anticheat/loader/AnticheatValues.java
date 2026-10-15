package dev.daweapon.anticheat.loader;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Configurable anticheat settings such as logging and ping-timeout tolerances.
public class AnticheatValues {
    private boolean shouldPrint = true;
    private int ticksAllowedToMissPing = 40;
    private int tickLength = 50;
}
