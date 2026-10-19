package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class FleeFromPlayersGoal extends GoalSelector {
	private static final long REPATH_MS = 400;
	private static final double[] ANGLES = {0, 0.9, -0.9, 1.8, -1.8};

	private final double triggerRange;
	private final double fleeDistance;
	private long lastPath;
	private boolean stop;

	public FleeFromPlayersGoal(EntityCreature entityCreature, double triggerRange, double fleeDistance) {
		super(entityCreature);
		this.triggerRange = triggerRange;
		this.fleeDistance = fleeDistance;
	}

	@Override
	public boolean shouldStart() {
		return EventMobSupport.nearestPlayer(entityCreature, triggerRange) != null;
	}

	@Override
	public void start() {
		stop = false;
	}

	@Override
	public void tick(long time) {
		SkyBlockPlayer threat = EventMobSupport.nearestPlayer(entityCreature, triggerRange * 1.5);
		if (threat == null) {
			stop = true;
			return;
		}
		if (time - lastPath < REPATH_MS) return;
		lastPath = time;
		Pos self = entityCreature.getPosition();
		double dx = self.x() - threat.getPosition().x();
		double dz = self.z() - threat.getPosition().z();
		double length = Math.max(0.001, Math.hypot(dx, dz));
		SkyBlockMob mob = (SkyBlockMob) entityCreature;
		for (double angle : ANGLES) {
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double rx = (dx * cos - dz * sin) / length * fleeDistance;
			double rz = (dx * sin + dz * cos) / length * fleeDistance;
			if (mob.getVanillaNavigator().pathTo(self.add(rx, 0, rz))) return;
		}
	}

	@Override
	public boolean shouldEnd() {
		return stop;
	}

	@Override
	public void end() {
		((SkyBlockMob) entityCreature).getVanillaNavigator().stop();
	}
}
