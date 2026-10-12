package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentPiercing;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentPunch;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentSnipe;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.handlers.bow.ArrowDamage;
import net.swofty.type.skyblockgeneric.item.handlers.bow.ArrowEffect;
import net.swofty.type.skyblockgeneric.item.handlers.bow.ArrowEffects;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.CombatPetHooks;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

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
                * CombatPetHooks.arrowMultiplier(shooter, collidedWith);
        boolean critical = hit.getValue();

        float damageAmount = (float) damage;
        if (!AttackService.applyHit(shooter, collidedWith, damageAmount, critical)) return;
        CombatPetHooks.bowHit(shooter);
        ArrowEffects.onHit(shooter, arrow, collidedWith, damageAmount, critical);
        punch(arrow, bow, collidedWith);
        ArrowDamage.onLanded(shooter, arrow);

        double ferocity = ArrowDamage.shooterStatistics(shooter, arrow.getArrowItem()).getOverall(ItemStatistic.FEROCITY);
        AttackService.scheduleExtraHits(shooter, collidedWith, damageAmount, critical, ferocity);
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
