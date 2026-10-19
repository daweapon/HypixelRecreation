package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;

final class EventMobSupport {
	private EventMobSupport() {
	}

	static SkyBlockPlayer nearestPlayer(Entity self, double range) {
		Instance instance = self.getInstance();
		if (instance == null) return null;
		return instance.getNearbyEntities(self.getPosition(), range).stream()
				.filter(entity -> entity instanceof SkyBlockPlayer player && !player.isRemoved() && !player.isDead())
				.map(entity -> (SkyBlockPlayer) entity)
				.min(Comparator.comparingDouble(player -> player.getDistanceSquared(self)))
				.orElse(null);
	}

	static void burst(Instance instance, Particle particle, Pos position, int count, float spread) {
		if (instance == null) return;
		ParticlePacket packet = new ParticlePacket(particle, true, true, position.x(), position.y(), position.z(),
				spread, spread, spread, 0.02f, count);
		instance.getPlayers().forEach(viewer -> viewer.sendPacket(packet));
	}

	static void trail(Instance instance, Particle particle, Pos from, Pos to, int points) {
		for (int i = 0; i <= points; i++) {
			double progress = (double) i / points;
			Pos at = new Pos(from.x() + (to.x() - from.x()) * progress,
					from.y() + (to.y() - from.y()) * progress,
					from.z() + (to.z() - from.z()) * progress);
			burst(instance, particle, at, 1, 0.02f);
		}
	}
}
