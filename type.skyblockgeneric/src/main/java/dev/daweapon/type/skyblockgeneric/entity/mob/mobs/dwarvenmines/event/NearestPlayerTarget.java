package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.TargetSelector;

public class NearestPlayerTarget extends TargetSelector {
	private final double range;

	public NearestPlayerTarget(EntityCreature entityCreature, double range) {
		super(entityCreature);
		this.range = range;
	}

	@Override
	public Entity findTarget() {
		return EventMobSupport.nearestPlayer(entityCreature, range);
	}
}
