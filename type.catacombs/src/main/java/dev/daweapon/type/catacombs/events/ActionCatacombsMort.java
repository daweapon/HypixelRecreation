package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.npc.CatacombsMortNPC;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.entity.EntityAttackEvent;

/** Hitting Mort opens his menu too, as in M7 run 1 where a left click on him opened Ready Up. */
public class ActionCatacombsMort implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;
        if (HypixelNPC.getFromImpl(player, event.getTarget()) instanceof CatacombsMortNPC) CatacombsMortNPC.talkTo(player);
    }
}
