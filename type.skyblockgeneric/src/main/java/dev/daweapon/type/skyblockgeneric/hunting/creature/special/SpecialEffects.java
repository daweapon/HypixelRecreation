package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

final class SpecialEffects {
    private SpecialEffects() {
    }

    static void burst(Entity source, Particle particle, Point at, int count, float spread) {
        source.sendPacketToViewers(new ParticlePacket(particle, false, false, (float) at.x(), (float) at.y(), (float) at.z(),
                spread, spread, spread, 0, count));
    }

    static void later(int ticks, Runnable action) {
        MinecraftServer.getSchedulerManager().buildTask(action).delay(TaskSchedule.tick(ticks)).schedule();
    }
}
