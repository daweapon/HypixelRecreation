package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.event;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.gui.inventory.item.GUIMaterial;
import dev.daweapon.type.skyblockgeneric.entity.mob.BestiaryMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.impl.MobPlayerSkin;
import dev.daweapon.type.skyblockgeneric.loottable.SkyBlockLootTable;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class RaidGoblin extends BestiaryMob implements MobPlayerSkin {
	private static final long PROTECTION_MESSAGE_INTERVAL_MS = 1500;
	private static final float PROTECTED_MELEE_FACTOR = 0.2f;

	private long lastProtectionMessage;

	protected RaidGoblin() {
		super(EntityType.PLAYER);
	}

	protected abstract double baseHealth();

	protected abstract double baseDamage();

	protected double aggroRange() {
		return 16;
	}

	@Override
	public void onSpawn() {
		setView(getPosition().yaw(), 90, getPosition().yaw());
	}

	@Override
	public float getNameDisplayHeightOffset() {
		return 0.1f;
	}

	@Override
	public int getMaxBestiaryTier() {
		return 15;
	}

	@Override
	public int getBestiaryBracket() {
		return 4;
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("172850906b7f0d952c0e508073cc439fd3374ccf5b889c06f7e8d90cc0cc255c");
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(new LastEntityDamagerTarget(this, 16), new NearestPlayerTarget(this, aggroRange()));
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of();
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, baseHealth())
				.withBase(ItemStatistic.DAMAGE, baseDamage())
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
	public List<MobType> getMobTypes() {
		return List.of(MobType.SUBTERRANEAN, MobType.HUMANOID);
	}

	@Override
	protected Damage filterIncomingDamage(Damage damage) {
		if (!MobSuperprotectron.isProtected(this)) return damage;
		warnAttacker(damage);
		if (isRangedHit()) return null;
		return new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(),
				damage.getAmount() * PROTECTED_MELEE_FACTOR);
	}

	private void warnAttacker(Damage damage) {
		if (!(damage.getAttacker() instanceof SkyBlockPlayer player)) return;
		long now = System.currentTimeMillis();
		if (now - lastProtectionMessage < PROTECTION_MESSAGE_INTERVAL_MS) return;
		lastProtectionMessage = now;
		player.sendMessage("<c>A nearby Superprotectron is defending this goblin!");
	}

	@Override
	public String getSkinTexture() {
		return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxNDEyODAyMCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFjMDNiMTRiN2EyYTBjMmY4MDkwMzAzNWJiZmRmODk4ZmViNDA1ZDdiZTM5ODBiNjdiYzQ5NmYxNTNhOTBkZCIKICAgIH0KICB9Cn0=";
	}

	@Override
	public String getSkinSignature() {
		return "YDiw8Yj4pk0TFQwGfiO8XEGp+Afq4O6IETqnQOrIweXiqo3kKTCXxkPMkM7ALWuWpIL+e/3neOWFLNujAbahF4SCpeYBeXN2KHg4BJW3KwBiDMBWlOaiyody8yx9/B20N/aHB1jRgZIWagi+flvlUhEd0ipb8Mmaz2VP03SD17q2DkX8DpjmQ2LFpUC4f4rwum+tY81d8zmr/htMUPWOoeWr7pmwGukvuE/0/1TnUBdXWzlviuTlII2CSNIs6MFoFZpiocQLtkdzHy+NEL8afqHFvKBD8OE5hIz84v01bFfgdGXvA5LIGr4b8WzvdbCQx6HlcnkliWZtTraWGTDPCca99rQRY2/Sy++uPd6Qf1gE22J3bIfjniTVufz2/VaDpkuVqW/MtKR+7ZQOit9vjVosINxFQN0ZNjFTrSF4NR9Tlcy3qEDdUtziuA7RK1Gj//KG9McRODXJXek8GCkHZep1RQd5VIG5/u/4c97vDez5O25fxdGDiyu7J0n/Rb+xBbhWCua4yJam2FjWAbW6oEFqqHGAH5+PnWDzjMBaGPAnxGcksFDxZ+0gj+1wm+fqfiasIdD2dCimOrMyBIkUE/egP7hbpt+Tr3CjIkdfdIzcIAOeip1UszmxVEUiCKBA5xrP7xK6Y9TXQObvk4TGbhMRdb5vYmSpDsAVMUNgk60=";
	}
}
