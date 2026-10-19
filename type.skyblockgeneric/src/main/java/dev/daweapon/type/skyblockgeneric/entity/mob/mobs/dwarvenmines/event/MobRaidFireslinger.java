package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class MobRaidFireslinger extends RaidGoblin {
	private static final long THROW_COOLDOWN_MS = 2500;
	private static final double THROW_RANGE = 12;
	private static final int FLIGHT_TICKS = 8;

	@Override
	public Integer getLevel() {
		return 3;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_WEAKLING_RANGED_5";
	}

	@Override
	public String getDisplayName() {
		return "Fireslinger";
	}

	@Override
	protected double baseHealth() {
		return 200;
	}

	@Override
	protected double baseDamage() {
		return 20;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new RangedAttackGoal(this, THROW_RANGE, THROW_COOLDOWN_MS, MobRaidFireslinger::throwFire));
	}

	private static void throwFire(SkyBlockMob mob, SkyBlockPlayer player) {
		if (mob.getInstance() == null) return;
		EventMobSupport.trail(mob.getInstance(), Particle.FLAME, mob.getPosition().add(0, 1.5, 0),
				player.getPosition().add(0, 1, 0), 12);
		mob.scheduler().buildTask(() -> {
			if (mob.isRemoved() || mob.isDead() || player.isRemoved() || player.getInstance() != mob.getInstance()) return;
			MobAttackService.strike(mob, player);
		}).delay(TaskSchedule.tick(FLIGHT_TICKS)).schedule();
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(5, 1, 3);
	}
}
