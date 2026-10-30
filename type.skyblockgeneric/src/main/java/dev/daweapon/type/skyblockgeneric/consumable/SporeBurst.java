package dev.daweapon.type.skyblockgeneric.consumable;

import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;

import java.util.concurrent.ThreadLocalRandom;

public final class SporeBurst {
    private SporeBurst() {
    }

    public static void explode(Instance instance, Point centre, double spread, int explosions) {
        if (instance == null) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < explosions; i++) {
            Pos blast = new Pos(centre.x() + (random.nextDouble() * 2 - 1) * spread, centre.y() + 0.5,
                    centre.z() + (random.nextDouble() * 2 - 1) * spread);
            MythologicalCombat.particles(instance, Particle.EXPLOSION, blast, 0.3f, 2);
            MythologicalCombat.particles(instance, Particle.CLOUD, blast, 0.8f, 12);
        }
    }
}
