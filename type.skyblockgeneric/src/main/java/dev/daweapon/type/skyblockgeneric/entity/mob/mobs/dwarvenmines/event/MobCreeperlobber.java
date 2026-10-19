package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ai.GoalSelector;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobCreeperlobber extends RaidGoblin {
	private static final long THROW_COOLDOWN_MS = 4500;
	private static final double THROW_RANGE = 14;
	private static final double THROW_SPEED = 14;
	private static final double THROW_LIFT = 7;

	@Override
	public Integer getLevel() {
		return 25;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_CREEPERTAMER_90";
	}

	@Override
	public String getDisplayName() {
		return "Creeperlobber";
	}

	@Override
	protected double baseHealth() {
		return 30000;
	}

	@Override
	protected double baseDamage() {
		return 10;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new RangedAttackGoal(this, THROW_RANGE, THROW_COOLDOWN_MS, MobCreeperlobber::throwCreeper));
	}

	private static void throwCreeper(SkyBlockMob mob, SkyBlockPlayer player) {
		if (mob.getInstance() == null) return;
		MobGoblinCreeper creeper = new MobGoblinCreeper();
		Vec direction = player.getPosition().sub(mob.getPosition()).asVec();
		Vec horizontal = new Vec(direction.x(), 0, direction.z());
		Vec velocity = (horizontal.length() < 0.01 ? Vec.ZERO : horizontal.normalize().mul(THROW_SPEED)).add(0, THROW_LIFT, 0);
		creeper.setInstance(mob.getInstance(), mob.getPosition().add(0, 1.5, 0)).thenRun(() -> creeper.setVelocity(velocity));
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
		return new SkyBlockLootTable() {
			@Override
			public @NonNull List<LootRecord> getLootTable() {
				return List.of(new LootRecord(ItemType.GUNPOWDER, 1, 50));
			}

			@Override
			public @NotNull CalculationMode getCalculationMode() {
				return CalculationMode.CALCULATE_INDIVIDUAL;
			}
		};
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(37, 8, 27);
	}
}
