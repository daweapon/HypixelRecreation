package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.time.TimeUnit;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.MeleeAttackWithinRegionGoal;
import dev.daweapon.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import dev.daweapon.type.skyblockgeneric.loottable.OtherLoot;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobRedstonePigman extends BestiaryMob implements RegionPopulator {
	private static final int REDSTONE_THRESHOLD = 64;
	private static final double PACK_RADIUS = 16;
	private static final double REDSTONE_SENSE_RADIUS = 8;
	private static final long WARNING_COOLDOWN_MILLIS = 10_000;
	private static final Map<UUID, Long> LAST_WARNING = new ConcurrentHashMap<>();

	private final Set<UUID> angryAt = ConcurrentHashMap.newKeySet();

	public MobRedstonePigman() {
		super(EntityType.ZOMBIFIED_PIGLIN);
	}

	@Override
	public String getDisplayName() {
		return "Redstone Pigman";
	}

	@Override
	public Integer getLevel() {
		return 8;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				new MeleeAttackWithinRegionGoal(this,
						1.6,
						20,
						TimeUnit.SERVER_TICK,
						RegionType.PIGMENS_DEN),
				new RandomRegionStrollGoal(this, 15, RegionType.PIGMENS_DEN)  // Walk around
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
				new LastEntityDamagerTarget(this, 12), // First target the last entity which attacked you
				new ClosestEntityRegionTarget(this,
						PACK_RADIUS,
						entity -> entity instanceof SkyBlockPlayer player && isAngryAt(player),
						RegionType.PIGMENS_DEN)
		);
	}

	public boolean isAngryAt(SkyBlockPlayer player) {
		return angryAt.contains(player.getUuid());
	}

	@Override
	public boolean damage(@NotNull Damage damage) {
		boolean hurt = super.damage(damage);
		if (hurt && damage.getAttacker() instanceof SkyBlockPlayer player) provokePack(player);
		return hurt;
	}

	@Override
	public void tick(long time) {
		super.tick(time);
		if (isDead() || getInstance() == null) return;
		long ticks = getAliveTicks();
		if (ticks % 20 == 0) senseRedstoneCarriers();
		if (ticks % 10 == 0 && !angryAt.isEmpty()) showAnger();
	}

	private void senseRedstoneCarriers() {
		for (var entity : getInstance().getNearbyEntities(getPosition(), REDSTONE_SENSE_RADIUS)) {
			if (entity instanceof SkyBlockPlayer player && !isAngryAt(player)
					&& player.getAmountInInventory(ItemType.REDSTONE) >= REDSTONE_THRESHOLD) {
				provokePack(player);
			}
		}
	}

	private void provokePack(SkyBlockPlayer player) {
		if (getInstance() == null) return;
		boolean newlyAngered = angryAt.add(player.getUuid());
		for (var entity : getInstance().getNearbyEntities(getPosition(), PACK_RADIUS)) {
			if (entity instanceof MobRedstonePigman pigman && pigman != this && !pigman.isDead()) {
				newlyAngered |= pigman.angryAt.add(player.getUuid());
			}
		}
		if (newlyAngered) warn(player);
	}

	private static void warn(SkyBlockPlayer player) {
		long now = System.currentTimeMillis();
		Long last = LAST_WARNING.get(player.getUuid());
		if (last != null && now - last < WARNING_COOLDOWN_MILLIS) return;
		LAST_WARNING.put(player.getUuid(), now);
		player.sendMessage("<c>The Redstone Pigmen are angry at you for taking their redstone!");
	}

	private void showAnger() {
		var position = getPosition();
		sendPacketToViewers(new ParticlePacket(Particle.FLAME, true, true, position.x(), position.y() + 1, position.z(),
				0.3f, 0.6f, 0.3f, 0.01f, 4));
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 250D)
				.withBase(ItemStatistic.DAMAGE, 75D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
		return new SkyBlockLootTable() {
			@Override
			public @NonNull List<LootRecord> getLootTable() {
				return List.of(
						new LootRecord(ItemType.PET_ITEM_EXP_SHARE_DROP, 1, 0.01),
						new LootRecord(ItemType.GOLD_NUGGET, 2, 50),
						new LootRecord(ItemType.REDSTONE, 5, 50),
						new LootRecord(ItemType.FLAMING_SWORD, 1, 1)
				);
			}

			@Override
			public @NotNull CalculationMode getCalculationMode() {
				return CalculationMode.CALCULATE_INDIVIDUAL;
			}
		};
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
		return new OtherLoot(12, 2, 8);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.UNDEAD);
	}

	@Override
	public int getMaxBestiaryTier() {
		return 5;
	}

	@Override
	public int getBestiaryBracket() {
		return 1;
	}

	@Override
	public String getMobID() {
		return "REDSTONE_PIGMAN";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb");
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(new Populator(RegionType.PIGMENS_DEN, 20));
	}
}
