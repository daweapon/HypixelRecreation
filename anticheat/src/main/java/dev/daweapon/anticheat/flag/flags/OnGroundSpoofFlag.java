package dev.daweapon.anticheat.flag.flags;

import dev.daweapon.anticheat.event.ListenerMethod;
import dev.daweapon.anticheat.event.events.PlayerPositionUpdateEvent;
import dev.daweapon.anticheat.flag.Flag;

// Intended to detect spoofed on-ground movement flags; currently disabled pending real collision checking.
public class OnGroundSpoofFlag extends Flag {
    @ListenerMethod
    public void onPlayerPositionUpdate(PlayerPositionUpdateEvent event) {
        // Disabled: heuristic-based detection has too many false positives
        // Needs actual collision checking against world blocks to be accurate
    }
}
