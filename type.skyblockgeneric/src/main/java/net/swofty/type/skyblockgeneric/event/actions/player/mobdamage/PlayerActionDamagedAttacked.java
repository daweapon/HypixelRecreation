package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.entity.EntityType;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.MobAttackService;

// Computes and applies damage dealt to a player by an attacking SkyBlock mob, including pet mitigation
public class PlayerActionDamagedAttacked implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (!event.getTarget().getEntityType().equals(EntityType.PLAYER)) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getTarget();

        if (event.getEntity() instanceof SkyBlockMob mob) {
            if (mob.getLastAttack() + mob.damageCooldown() > System.currentTimeMillis()) return;
            mob.setLastAttack(System.currentTimeMillis());

            MobAttackService.strike(mob, player);
            mob.onMeleeHit(player);
        }
    }

}
