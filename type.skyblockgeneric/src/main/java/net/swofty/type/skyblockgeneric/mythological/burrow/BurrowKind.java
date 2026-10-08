package net.swofty.type.skyblockgeneric.mythological.burrow;

import lombok.Getter;
import net.minestom.server.particle.Particle;

import java.util.random.RandomGenerator;

@Getter
public enum BurrowKind {
    START(Particle.ENCHANTED_HIT, 4, 0.5f),
    MOB(Particle.CRIT, 3, 0.5f),
    TREASURE(Particle.DRIPPING_LAVA, 2, 0.35f);

    private static final double MOB_CHANCE = 0.75;

    private final Particle particle;
    private final int particleCount;
    private final float horizontalSpread;

    BurrowKind(Particle particle, int particleCount, float horizontalSpread) {
        this.particle = particle;
        this.particleCount = particleCount;
        this.horizontalSpread = horizontalSpread;
    }

    public static BurrowKind rollFollowUp(RandomGenerator random) {
        return random.nextDouble() < MOB_CHANCE ? MOB : TREASURE;
    }
}
