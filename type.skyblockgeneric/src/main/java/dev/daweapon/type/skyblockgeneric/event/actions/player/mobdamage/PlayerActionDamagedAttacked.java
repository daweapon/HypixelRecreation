package dev.daweapon.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.entity.EntityType;
import net.minestom.server.event.entity.EntityAttackEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;

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
