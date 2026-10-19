package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;

import java.util.List;

public class MobSuperprotectron extends RaidGoblin {
	public static final double PROTECTION_RADIUS = 8;
	public static final int HITS_TO_KILL = 12;

	public static boolean isProtected(SkyBlockMob mob) {
		if (mob instanceof MobSuperprotectron || mob.getInstance() == null) return false;
		for (SkyBlockMob other : SkyBlockMob.getMobs()) {
			if (other instanceof MobSuperprotectron guard && !guard.isDead() && !guard.isRemoved()
					&& guard.getInstance() == mob.getInstance()
					&& guard.getPosition().distanceSquared(mob.getPosition()) <= PROTECTION_RADIUS * PROTECTION_RADIUS) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Integer getLevel() {
		return 95;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_GOLEM_150";
	}

	@Override
	public String getDisplayName() {
		return "Superprotectron";
	}

	@Override
	protected double baseHealth() {
		return HITS_TO_KILL;
	}

	@Override
	protected double baseDamage() {
		return 50;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new VanillaMeleeAttackGoal(this, 2, 20, TimeUnit.SERVER_TICK));
	}

	@Override
	protected Damage filterIncomingDamage(Damage damage) {
		return HitLimit.convert(this, damage, HITS_TO_KILL);
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(142, 31, 104);
	}
}
