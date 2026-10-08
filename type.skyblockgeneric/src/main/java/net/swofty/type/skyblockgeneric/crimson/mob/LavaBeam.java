package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;

public final class LavaBeam {
    private LavaBeam() {
    }

    public static void draw(Instance instance, Point from, Point to) {
        draw(instance, from, to, Particle.FLAME);
    }

    public static void draw(Instance instance, Point from, Point to, Particle particle) {
        if (instance == null) return;
        Vec step = new Vec(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
        double length = step.length();
        if (length < 0.01) return;
        Vec unit = step.div(length);
        for (double travelled = 0; travelled <= length; travelled += 0.6) {
            MythologicalCombat.particles(instance, particle, new Pos(from.x(), from.y(), from.z()).add(unit.mul(travelled)), 0f, 1);
        }
    }

    public static void ring(Instance instance, Point centre, double radius, Particle particle) {
        if (instance == null) return;
        int points = Math.max(16, (int) (radius * 8));
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            MythologicalCombat.particles(instance, particle,
                    new Pos(centre.x() + Math.cos(angle) * radius, centre.y() + 0.2, centre.z() + Math.sin(angle) * radius), 0f, 1);
        }
    }
}
