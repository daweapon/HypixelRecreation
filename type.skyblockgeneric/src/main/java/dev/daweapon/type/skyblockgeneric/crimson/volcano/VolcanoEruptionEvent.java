package dev.daweapon.type.skyblockgeneric.crimson.volcano;

import net.minestom.server.event.Event;

/** Fired on the Crimson Isle server when the Blazing Volcano starts ({@code started}) or stops erupting. */
public record VolcanoEruptionEvent(VolcanoExplosivity explosivity, boolean started) implements Event {
}
