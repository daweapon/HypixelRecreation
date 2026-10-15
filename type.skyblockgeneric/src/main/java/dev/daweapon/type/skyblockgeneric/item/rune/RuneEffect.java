package dev.daweapon.type.skyblockgeneric.item.rune;

import net.minestom.server.particle.Particle;

public record RuneEffect(Particle particle, Trigger trigger, int perLevel, float spread) {
    public enum Trigger {
        KILL,
        MOVE,
        ARROW
    }

    public int count(int runeLevel) {
        return Math.max(1, runeLevel) * perLevel;
    }
}
