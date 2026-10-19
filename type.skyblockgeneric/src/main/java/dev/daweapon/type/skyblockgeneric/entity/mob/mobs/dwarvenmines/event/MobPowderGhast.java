package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobPowderGhast extends BestiaryMob {
	private static final int DESPAWN_SECONDS = 60;
	private static final int MIN_POWDER = 100;
	private static final int MAX_POWDER = 250;
	private static final long IMMUNE_MESSAGE_INTERVAL_MS = 2000;

	private Task despawnTask;
	private long lastImmuneMessage;

	public MobPowderGhast() {
		super(EntityType.GHAST);
	}

	@Override
	public void onInit() {
		setNoGravity(true);
	}

	@Override
	public void onSpawn() {
		resetDespawn();
	}

	private void resetDespawn() {
		if (despawnTask != null) despawnTask.cancel();
		despawnTask = scheduler().buildTask(this::remove).delay(TaskSchedule.seconds(DESPAWN_SECONDS)).schedule();
	}

	@Override
	protected Damage filterIncomingDamage(Damage damage) {
		if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return damage;
		if (!isRangedHit()) {
			long now = System.currentTimeMillis();
			if (now - lastImmuneMessage >= IMMUNE_MESSAGE_INTERVAL_MS) {
				lastImmuneMessage = now;
				player.sendMessage("<c>The Powder Ghast is immune to melee damage! Try arrows!");
			}
			return null;
		}
		resetDespawn();
		long amount = ThreadLocalRandom.current().nextLong(MIN_POWDER, MAX_POWDER + 1);
		long gained = HotmService.gainPowder(player, HotmPowder.MITHRIL, amount);
		player.sendMessage("<a>+{:,} {}", gained, HotmPowder.MITHRIL.coloured());
		return damage;
	}

	@Override
	public Integer getLevel() {
		return 55;
	}

	@Override
	public String getMobID() {
		return "POWDER_GHAST";
	}

	@Override
	public String getDisplayName() {
		return "Powder Ghast";
	}

	@Override
	public int getMaxBestiaryTier() {
		return 15;
	}

	@Override
	public int getBestiaryBracket() {
		return 8;
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("de8a38e9afbd3da10d19b577c55c7bfd6b4f2e407e44d4017b23be9167abff02");
	}

	@Override
	public float getNameDisplayHeightOffset() {
		return 2.2f;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(new FlyAwayGoal(this, 12, 6));
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of();
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 50D)
				.withBase(ItemStatistic.DAMAGE, 1D)
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
		return new OtherLoot(82, 18, 60);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.AIRBORNE);
	}
}
