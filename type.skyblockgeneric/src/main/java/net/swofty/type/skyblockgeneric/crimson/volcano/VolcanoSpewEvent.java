package net.swofty.type.skyblockgeneric.crimson.volcano;

import net.minestom.server.event.Event;

/**
 * Fired in {@code waves} even steps over an eruption while the volcano spews blocks; listeners place their
 * {@link #share(int)} of the eruption's total each time.
 */
public record VolcanoSpewEvent(VolcanoExplosivity explosivity, int wave, int waves) implements Event {
    public int share(int total) {
        return (int) ((long) total * (wave + 1) / waves - (long) total * wave / waves);
    }
}
