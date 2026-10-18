package dev.daweapon.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentPiercing;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentPunch;
import dev.daweapon.type.skyblockgeneric.enchantment.impl.EnchantmentSnipe;
import dev.daweapon.type.skyblockgeneric.entity.ArrowEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.ArrowDamage;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.ArrowEffect;
import dev.daweapon.type.skyblockgeneric.item.handlers.bow.ArrowEffects;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

import java.util.Map;

// Computes and applies arrow damage when a player's shot arrow collides with a SkyBlock mob
public class PlayerActionArrowDamageMob implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(ProjectileCollideWithEntityEvent event) {
        ArrowEntityImpl arrow;
        if (event.getEntity() instanceof ArrowEntityImpl arrowEntity)
            arrow = arrowEntity;
        else return;

        SkyBlockMob collidedWith;
        if (event.getTarget() instanceof SkyBlockMob mob)
            collidedWith = mob;
        else return;
        if (collidedWith.isImmuneToArrows()) return;

        SkyBlockPlayer shooter;
        if (arrow.getShooter() instanceof SkyBlockPlayer player)
            shooter = player;
        else return;

        ArrowEffect effect = ArrowEffects.of(arrow.getArrowItem());
        if (effect == ArrowEffect.EXPLOSIVE) {
            ArrowEffects.explode(arrow, collidedWith.getPosition());
            return;
        }

        SkyBlockItem bow = arrow.getBow();
        double pierceFactor = 1;
        if (bow != null && bow.getAttributeHandler().getEnchantment(EnchantmentType.PIERCING) != null) {
            event.setCancelled(true);
            int targetsAlreadyHit = arrow.pierceTarget(collidedWith);
            if (targetsAlreadyHit < 0) return;
            pierceFactor = ArrowEffects.pierceFactor(effect, targetsAlreadyHit, EnchantmentPiercing.damageFactor(targetsAlreadyHit));
            if (pierceFactor <= 0) return;
        }

        ItemStatistics entityStats = mob.getStatistics();
        Map.Entry<Double, Boolean> hit = ArrowDamage.roll(shooter, arrow, entityStats);
        double damage = hit.getKey() * pierceFactor * snipeMultiplier(arrow, bow, collidedWith)
                * AttributeEffectService.outgoingDamageMultiplier(shooter.getHuntingData(), collidedWith, true)
                * (1 + AttributeEffectService.lifelinePercent(shooter) / 100D)
                * CombatPetHooks.arrowMultiplier(shooter, collidedWith);
        boolean critical = hit.getValue();

        float damageAmount = (float) damage;
        if (!AttackService.applyHit(shooter, collidedWith, damageAmount, critical)) return;
        CombatPetHooks.bowHit(shooter);
        dev.daweapon.type.skyblockgeneric.item.armor.ArmorHitEffects.onArrowHit(shooter, collidedWith);
        ArrowEffects.onHit(shooter, arrow, collidedWith, damageAmount, critical);
        punch(arrow, bow, collidedWith);
        dev.daweapon.type.skyblockgeneric.item.handlers.bow.ArrowBounce.chain(shooter, bow, collidedWith, damageAmount, critical);
        ArrowDamage.onLanded(shooter, arrow);
        bowHitEnchants(shooter, bow, collidedWith, damageAmount);

        double ferocity = ArrowDamage.shooterStatistics(shooter, arrow.getArrowItem()).getOverall(ItemStatistic.FEROCITY);
        AttackService.scheduleExtraHits(shooter, collidedWith, damageAmount, critical, ferocity);
    }

    private static void bowHitEnchants(SkyBlockPlayer shooter, SkyBlockItem bow, SkyBlockMob target, double damage) {
        if (bow == null) return;
        for (SkyBlockEnchantment enchantment : bow.getAttributeHandler().getEnchantments().toList()) {
            if (enchantment.type().getEnch() instanceof dev.daweapon.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant onHit) {
                onHit.onDamageDealt(shooter, target, damage,
                        dev.daweapon.type.skyblockgeneric.reforge.ReforgeLoadout.effectiveEnchantLevel(shooter, enchantment.type(), enchantment.level()));
            }
        }
    }

    private static double snipeMultiplier(ArrowEntityImpl arrow, SkyBlockItem bow, SkyBlockMob target) {
        if (bow == null || arrow.getOrigin() == null) return 1;
        SkyBlockEnchantment snipe = bow.getAttributeHandler().getEnchantment(EnchantmentType.SNIPE);
        if (snipe == null) return 1;
        return EnchantmentSnipe.multiplier(snipe.level(), arrow.getOrigin().distance(target.getPosition()));
    }

    private static void punch(ArrowEntityImpl arrow, SkyBlockItem bow, SkyBlockMob target) {
        if (bow == null) return;
        SkyBlockEnchantment punch = bow.getAttributeHandler().getEnchantment(EnchantmentType.PUNCH);
        if (punch == null) return;
        var velocity = arrow.getVelocity();
        double length = Math.hypot(velocity.x(), velocity.z());
        if (length == 0) return;
        target.takeKnockback(EnchantmentPunch.knockback(punch.level()), -velocity.x() / length, -velocity.z() / length);
    }
}
