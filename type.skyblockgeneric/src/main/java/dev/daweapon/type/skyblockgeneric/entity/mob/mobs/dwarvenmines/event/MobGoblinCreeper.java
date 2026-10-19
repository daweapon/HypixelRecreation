package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobGoblinCreeper extends SkyBlockMob {
	private static final int FUSE_TICKS = 30;
	private static final double BLAST_RADIUS = 3.5;
	private static final double FLAT_DAMAGE = 50;
	private static final double MAX_HEALTH_FRACTION = 0.2;

	private boolean detonated;

	public MobGoblinCreeper() {
		super(EntityType.CREEPER);
	}

	@Override
	public void onSpawn() {
		scheduler().buildTask(this::detonate).delay(TaskSchedule.tick(FUSE_TICKS)).schedule();
	}

	private void detonate() {
		if (detonated || isRemoved() || getInstance() == null) return;
		detonated = true;
		EventMobSupport.burst(getInstance(), Particle.EXPLOSION, getPosition().add(0, 0.5, 0), 3, 0.6f);
		getInstance().getNearbyEntities(getPosition(), BLAST_RADIUS).stream()
				.filter(entity -> entity instanceof SkyBlockPlayer player && !player.isDead())
				.map(entity -> (SkyBlockPlayer) entity)
				.forEach(player -> MobAttackService.trueStrike(this, player,
						FLAT_DAMAGE + MAX_HEALTH_FRACTION * player.getMaxHealth()));
		remove();
	}

	@Override
	public boolean damage(@NotNull Damage damage) {
		if (!isRemoved()) remove();
		return true;
	}

	@Override
	public String getDisplayName() {
		return "Creeper";
	}

	@Override
	public Integer getLevel() {
		return 1;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of();
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of();
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 1D)
				.withBase(ItemStatistic.DAMAGE, 0D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
		return null;
	}

	@Override
	public SkillCategories getSkillCategory() {
		return SkillCategories.COMBAT;
	}

	@Override
	public long damageCooldown() {
		return 500;
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(0, 0, 0);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.CONSTRUCT);
	}
}
