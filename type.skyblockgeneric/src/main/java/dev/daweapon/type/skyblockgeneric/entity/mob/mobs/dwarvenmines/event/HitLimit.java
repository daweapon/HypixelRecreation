package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.damage.Damage;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

final class HitLimit {
	private HitLimit() {
	}

	static Damage convert(SkyBlockMob mob, Damage damage, int hitsToKill) {
		if (AttackService.isExtraHit()) return null;
		float perHit = mob.getMaxHealth() / hitsToKill;
		boolean lastHit = mob.getHealth() <= perHit * 1.001f;
		float amount = lastHit ? mob.getHealth() : perHit;
		return new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(), amount);
	}
}
