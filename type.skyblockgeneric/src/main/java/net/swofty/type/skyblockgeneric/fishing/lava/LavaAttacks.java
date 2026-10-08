package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.Optional;

final class LavaAttacks {
    private LavaAttacks() {
    }

    static Optional<SkyBlockPlayer> target(LavaSeaCreatureMob mob, double range) {
        SkyBlockPlayer angler = mob.getAngler();
        if (angler != null && angler.getInstance() == mob.getInstance() && !angler.isDead()
                && !PeaceTreaty.applies(angler) && angler.getPosition().distance(mob.getPosition()) <= range) {
            return Optional.of(angler);
        }
        return mob.playersWithin(range).stream()
                .filter(player -> !PeaceTreaty.applies(player))
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(mob.getPosition())));
    }

    static void beam(Instance instance, Point from, Point to, Particle particle) {
        Vec step = new Vec(to.x() - from.x(), to.y() - from.y(), to.z() - from.z());
        double length = step.length();
        if (length < 0.01) return;
        Vec unit = step.div(length);
        for (double travelled = 0; travelled <= length; travelled += 0.5) {
            MythologicalCombat.particles(instance, particle, new Pos(from.x(), from.y(), from.z()).add(unit.mul(travelled)), 0f, 1);
        }
    }

    static void ring(Instance instance, Point centre, double radius, Particle particle) {
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24;
            MythologicalCombat.particles(instance, particle,
                    new Pos(centre.x() + Math.cos(angle) * radius, centre.y() + 0.1, centre.z() + Math.sin(angle) * radius), 0f, 1);
        }
    }
}
