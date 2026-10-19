package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

import java.util.List;

public class MobMurderlover extends RaidGoblin {
	private static final long HOOK_COOLDOWN_MS = 3000;
	private static final double HOOK_RANGE = 10;
	private static final double PULL_STRENGTH = 0.35;

	@Override
	public Integer getLevel() {
		return 47;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_MURDERLOVER_150";
	}

	@Override
	public String getDisplayName() {
		return "Murderlover";
	}

	@Override
	protected double baseHealth() {
		return 50000;
	}

	@Override
	protected double baseDamage() {
		return 50;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new RangedAttackGoal(this, HOOK_RANGE, HOOK_COOLDOWN_MS, MobMurderlover::castHook));
	}

	private static void castHook(SkyBlockMob mob, SkyBlockPlayer player) {
		if (mob.getInstance() == null) return;
		EventMobSupport.trail(mob.getInstance(), Particle.CRIT, mob.getPosition().add(0, 1.5, 0),
				player.getPosition().add(0, 1, 0), 14);
		MobAttackService.strike(mob, player);
		Vec pull = mob.getPosition().sub(player.getPosition()).asVec().mul(PULL_STRENGTH);
		player.setVelocity(new Vec(pull.x(), 4, pull.z()));
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(70, 15, 51);
	}
}
