package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class FlyAwayGoal extends GoalSelector {
	private final double triggerRange;
	private final double speed;
	private boolean stop;

	public FlyAwayGoal(EntityCreature entityCreature, double triggerRange, double speed) {
		super(entityCreature);
		this.triggerRange = triggerRange;
		this.speed = speed;
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
		Vec away = entityCreature.getPosition().sub(threat.getPosition()).asVec();
		Vec horizontal = new Vec(away.x(), 0, away.z());
		Vec direction = horizontal.length() < 0.01 ? new Vec(1, 0, 0) : horizontal.normalize();
		entityCreature.setVelocity(direction.mul(speed).add(0, 1.5, 0));
	}

	@Override
	public boolean shouldEnd() {
		return stop;
	}

	@Override
	public void end() {
	}
}
