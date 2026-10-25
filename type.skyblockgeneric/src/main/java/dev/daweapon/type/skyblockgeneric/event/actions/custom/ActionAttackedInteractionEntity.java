package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.event.entity.EntityAttackEvent;

public class ActionAttackedInteractionEntity implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (event.getEntity() instanceof HypixelPlayer player && event.getTarget() instanceof InteractionEntity interaction) {
            interaction.attacked(player);
        }
    }
}
