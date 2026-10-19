package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.ai.GoalSelector;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;

import java.util.List;

public class MobRaidWeakling extends RaidGoblin {

	@Override
	public Integer getLevel() {
		return 3;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_WEAKLING_MELEE_5";
	}

	@Override
	public String getDisplayName() {
		return "Weakling";
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
		return List.of(new FleeFromPlayersGoal(this, 8, 12));
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(5, 1, 3);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.SUBTERRANEAN, MobType.HUMANOID, MobType.TIMID);
	}
}
