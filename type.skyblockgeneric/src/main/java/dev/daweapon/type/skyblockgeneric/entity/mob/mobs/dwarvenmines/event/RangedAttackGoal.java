package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BiConsumer;

public class RangedAttackGoal extends GoalSelector {
	private static final long REPATH_MS = 500;

	private final double range;
	private final long cooldownMs;
	private final BiConsumer<SkyBlockMob, SkyBlockPlayer> attack;
	private long lastAttack;
	private long lastPath;
	private boolean stop;

	public RangedAttackGoal(EntityCreature entityCreature, double range, long cooldownMs,
							BiConsumer<SkyBlockMob, SkyBlockPlayer> attack) {
		super(entityCreature);
		this.range = range;
		this.cooldownMs = cooldownMs;
		this.attack = attack;
	}

	@Override
	public boolean shouldStart() {
		return findTarget() != null;
	}

	@Override
	public void start() {
		stop = false;
	}

	@Override
	public void tick(long time) {
		Entity target = findTarget();
		if (target == null || entityCreature.isDead()) {
			stop = true;
			return;
		}
		SkyBlockMob mob = (SkyBlockMob) entityCreature;
		if (entityCreature.getDistanceSquared(target) <= range * range) {
			mob.getVanillaNavigator().stop();
			entityCreature.lookAt(target);
			if (time - lastAttack >= cooldownMs && target instanceof SkyBlockPlayer player) {
				lastAttack = time;
				attack.accept(mob, player);
			}
		} else if (time - lastPath >= REPATH_MS) {
			lastPath = time;
			mob.getVanillaNavigator().pathTo(target.getPosition());
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
