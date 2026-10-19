package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;

import java.util.List;

public class MobPitfighter extends RaidGoblin {

	@Override
	public Integer getLevel() {
		return 31;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_BATTLER_60";
	}

	@Override
	public String getDisplayName() {
		return "Pitfighter";
	}

	@Override
	protected double baseHealth() {
		return 30000;
	}

	@Override
	protected double baseDamage() {
		return 20;
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, baseHealth())
				.withBase(ItemStatistic.DAMAGE, baseDamage())
				.withBase(ItemStatistic.SPEED, 70D)
				.build();
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new VanillaMeleeAttackGoal(this, 1.8, 30, TimeUnit.SERVER_TICK));
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(46, 10, 34);
	}
}
