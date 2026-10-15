package dev.daweapon.pvp.feature;

import dev.daweapon.pvp.feature.armor.ArmorFeature;
import dev.daweapon.pvp.feature.attack.AttackFeature;
import dev.daweapon.pvp.feature.attack.CriticalFeature;
import dev.daweapon.pvp.feature.attack.SweepingFeature;
import dev.daweapon.pvp.feature.attributes.EquipmentFeature;
import dev.daweapon.pvp.feature.block.BlockFeature;
import dev.daweapon.pvp.feature.block.LegacyBlockFeature;
import dev.daweapon.pvp.feature.cooldown.AttackCooldownFeature;
import dev.daweapon.pvp.feature.cooldown.ItemCooldownFeature;
import dev.daweapon.pvp.feature.damage.DamageFeature;
import dev.daweapon.pvp.feature.effect.EffectFeature;
import dev.daweapon.pvp.feature.enchantment.EnchantmentFeature;
import dev.daweapon.pvp.feature.explosion.ExplosionFeature;
import dev.daweapon.pvp.feature.explosion.ExplosiveFeature;
import dev.daweapon.pvp.feature.fall.FallFeature;
import dev.daweapon.pvp.feature.food.ExhaustionFeature;
import dev.daweapon.pvp.feature.food.FoodFeature;
import dev.daweapon.pvp.feature.food.RegenerationFeature;
import dev.daweapon.pvp.feature.item.ItemDamageFeature;
import dev.daweapon.pvp.feature.knockback.KnockbackFeature;
import dev.daweapon.pvp.feature.potion.PotionFeature;
import dev.daweapon.pvp.feature.projectile.BowFeature;
import dev.daweapon.pvp.feature.projectile.CrossbowFeature;
import dev.daweapon.pvp.feature.projectile.FireworkRocketFeature;
import dev.daweapon.pvp.feature.projectile.FishingRodFeature;
import dev.daweapon.pvp.feature.projectile.MiscProjectileFeature;
import dev.daweapon.pvp.feature.projectile.ProjectileItemFeature;
import dev.daweapon.pvp.feature.projectile.TridentFeature;
import dev.daweapon.pvp.feature.provider.DifficultyProvider;
import dev.daweapon.pvp.feature.provider.PacketProvider;
import dev.daweapon.pvp.feature.provider.SoundProvider;
import dev.daweapon.pvp.feature.spectate.SpectateFeature;
import dev.daweapon.pvp.feature.state.PlayerStateFeature;
import dev.daweapon.pvp.feature.totem.TotemFeature;
import dev.daweapon.pvp.feature.tracking.TrackingFeature;
import dev.daweapon.pvp.utils.CombatVersion;

/**
 * Represents a type of {@link CombatFeature}.
 *
 * @param name the name of the feature
 * @param defaultFeature the default instance of the feature (no op)
 * @param <F> the class of the feature
 */
public record FeatureType<F extends CombatFeature>(String name, F defaultFeature) {
	public static final FeatureType<CombatVersion> VERSION = of("VERSION", CombatVersion.MODERN);
	public static final FeatureType<ArmorFeature> ARMOR = of("ARMOR", ArmorFeature.NO_OP);
	public static final FeatureType<AttackFeature> ATTACK = of("ATTACK", AttackFeature.NO_OP);
	public static final FeatureType<CriticalFeature> CRITICAL = of("CRITICAL", CriticalFeature.NO_OP);
	public static final FeatureType<SweepingFeature> SWEEPING = of("SWEEPING", SweepingFeature.NO_OP);
	public static final FeatureType<EquipmentFeature> EQUIPMENT = of("EQUIPMENT_DATA", EquipmentFeature.NO_OP);
	public static final FeatureType<BlockFeature> BLOCK = of("BLOCK", BlockFeature.NO_OP);
	public static final FeatureType<LegacyBlockFeature> LEGACY_BLOCK = of("LEGACY_BLOCK", LegacyBlockFeature.NO_OP);
	public static final FeatureType<AttackCooldownFeature> ATTACK_COOLDOWN = of("ATTACK_COOLDOWN", AttackCooldownFeature.NO_OP);
	public static final FeatureType<ItemCooldownFeature> ITEM_COOLDOWN = of("ITEM_COOLDOWN", ItemCooldownFeature.NO_OP);
	public static final FeatureType<DamageFeature> DAMAGE = of("DAMAGE", DamageFeature.NO_OP);
	public static final FeatureType<EnchantmentFeature> ENCHANTMENT = of("ENCHANTMENT", EnchantmentFeature.NO_OP);
	public static final FeatureType<EffectFeature> EFFECT = of("EFFECT", EffectFeature.NO_OP);
	public static final FeatureType<ExplosionFeature> EXPLOSION = of("EXPLOSION", ExplosionFeature.NO_OP);
	public static final FeatureType<ExplosiveFeature> EXPLOSIVE = of("EXPLOSIVE", ExplosiveFeature.NO_OP);
	public static final FeatureType<FallFeature> FALL = of("FALL", FallFeature.NO_OP);
	public static final FeatureType<ExhaustionFeature> EXHAUSTION = of("EXHAUSTION", ExhaustionFeature.NO_OP);
	public static final FeatureType<FoodFeature> FOOD = of("FOOD", FoodFeature.NO_OP);
	public static final FeatureType<RegenerationFeature> REGENERATION = of("REGENERATION", RegenerationFeature.NO_OP);
	public static final FeatureType<ItemDamageFeature> ITEM_DAMAGE = of("ITEM_DAMAGE", ItemDamageFeature.NO_OP);
	public static final FeatureType<KnockbackFeature> KNOCKBACK = of("KNOCKBACK", KnockbackFeature.NO_OP);
	public static final FeatureType<PotionFeature> POTION = of("POTION", PotionFeature.NO_OP);
	public static final FeatureType<BowFeature> BOW = of("BOW", BowFeature.NO_OP);
	public static final FeatureType<CrossbowFeature> CROSSBOW = of("CROSSBOW", CrossbowFeature.NO_OP);
	public static final FeatureType<FishingRodFeature> FISHING_ROD = of("FISHING_ROD", FishingRodFeature.NO_OP);
	public static final FeatureType<MiscProjectileFeature> MISC_PROJECTILE = of("ITEM_PROJECTILE", MiscProjectileFeature.NO_OP);
	public static final FeatureType<ProjectileItemFeature> PROJECTILE_ITEM = of("PROJECTILE_ITEM", ProjectileItemFeature.NO_OP);
	public static final FeatureType<TridentFeature> TRIDENT = of("TRIDENT", TridentFeature.NO_OP);
	public static final FeatureType<DifficultyProvider> DIFFICULTY = of("DIFFICULTY", DifficultyProvider.DEFAULT);
	public static final FeatureType<PacketProvider> PACKET = of("PACKET", PacketProvider.DEFAULT);
	public static final FeatureType<SoundProvider> SOUND = of("SOUND", SoundProvider.DEFAULT);
	public static final FeatureType<SpectateFeature> SPECTATE = of("SPECTATE", SpectateFeature.NO_OP);
	public static final FeatureType<PlayerStateFeature> PLAYER_STATE = of("PLAYER_STATE", PlayerStateFeature.NO_OP);
	public static final FeatureType<TotemFeature> TOTEM = of("TOTEM", TotemFeature.NO_OP);
	public static final FeatureType<TrackingFeature> TRACKING = of("TRACKING", TrackingFeature.NO_OP);
	public static final FeatureType<FireworkRocketFeature> FIREWORK = of("FIREWORK", FireworkRocketFeature.NO_OP);

	public static <F extends CombatFeature> FeatureType<F> of(String name, F noopFeature) {
		return new FeatureType<>(name, noopFeature);
	}
}
