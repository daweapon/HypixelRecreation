package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobStarSentry extends BestiaryMob {
	public static final int HITS_TO_KILL = 10;
	private static final double AGGRO_RANGE = 5;
	private static final int MITHRIL_PER_HIT = 5;
	private static final double STARFALL_CHANCE_PERCENT = 20;

	public MobStarSentry() {
		super(EntityType.IRON_GOLEM);
	}

	@Override
	public Integer getLevel() {
		return 135;
	}

	@Override
	public String getMobID() {
		return "STAR_SENTRY";
	}

	@Override
	public String getDisplayName() {
		return "Star Sentry";
	}

	@Override
	public int getMaxBestiaryTier() {
		return 15;
	}

	@Override
	public int getBestiaryBracket() {
		return 6;
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("d87b8f3cf1c1014883276f230501d5af1397699071bfab1ee294c861722cc2f");
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new VanillaMeleeAttackGoal(this, 2.5, 20, TimeUnit.SERVER_TICK));
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(new NearestPlayerTarget(this, AGGRO_RANGE));
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, (double) HITS_TO_KILL)
				.withBase(ItemStatistic.DAMAGE, 400D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	protected Damage filterIncomingDamage(Damage damage) {
		Damage converted = HitLimit.convert(this, damage, HITS_TO_KILL);
		if (converted != null && damage.getAttacker() instanceof SkyBlockPlayer player) awardHit(player);
		return converted;
	}

	private void awardHit(SkyBlockPlayer player) {
		dev.daweapon.type.skyblockgeneric.commission.CommissionProgress.update(player,
				dev.daweapon.type.skyblockgeneric.commission.ObjectiveType.DAMAGE,
				dev.daweapon.type.skyblockgeneric.commission.Objective.BlockTarget.STAR_SENTRY, 1, null);
		player.addAndUpdateItem(ItemType.MITHRIL, MITHRIL_PER_HIT);
		if (LootRoll.roll(player.getUuid(), ItemType.STARFALL, STARFALL_CHANCE_PERCENT)) {
			player.addAndUpdateItem(ItemType.STARFALL, 1);
		}
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
		return new OtherLoot(202, 45, 148);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.CONSTRUCT, MobType.SHIELDED);
	}
}
