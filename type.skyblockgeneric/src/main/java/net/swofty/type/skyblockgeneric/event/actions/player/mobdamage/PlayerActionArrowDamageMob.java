package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.handlers.bow.ArrowDamage;
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

        ItemStatistics entityStats = mob.getStatistics();
        Map.Entry<Double, Boolean> hit = ArrowDamage.roll(shooter, arrow, entityStats);
        double damage = hit.getKey() * AttributeEffectService.outgoingDamageMultiplier(shooter.getHuntingData(), collidedWith, true)
                * CombatPetHooks.arrowMultiplier(shooter, collidedWith);
        boolean critical = hit.getValue();

        float damageAmount = (float) damage;
        if (!AttackService.applyHit(shooter, collidedWith, damageAmount, critical)) return;
        CombatPetHooks.bowHit(shooter);
        ArrowDamage.onLanded(shooter, arrow);

        double ferocity = ArrowDamage.shooterStatistics(shooter, arrow.getArrowItem()).getOverall(ItemStatistic.FEROCITY);
        AttackService.scheduleExtraHits(shooter, collidedWith, damageAmount, critical, ferocity);
    }
}
