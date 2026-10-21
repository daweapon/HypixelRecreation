package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.entity.PlayerHand;

// Lets a hunting creature's special react to a right-click
public class ActionCreatureInteract implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;
        if (!(event.getTarget() instanceof HuntingCreatureMob mob) || mob.isDead()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        mob.special().ifPresent(special -> special.onInteract(player, mob));
    }
}
