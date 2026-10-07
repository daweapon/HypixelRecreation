package net.swofty.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import net.minestom.server.particle.Particle;

import java.util.random.RandomGenerator;

@Getter
public enum BurrowKind {
    START(Particle.ENCHANTED_HIT),
    MOB(Particle.CRIT),
    TREASURE(Particle.DRIPPING_LAVA);

    private static final double MOB_CHANCE = 0.75;

    private final Particle particle;

    BurrowKind(Particle particle) {
        this.particle = particle;
    }

    public static BurrowKind rollFollowUp(RandomGenerator random) {
        return random.nextDouble() < MOB_CHANCE ? MOB : TREASURE;
    }
}
