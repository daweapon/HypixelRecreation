package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.run.RewardChestStand;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;

/** Right-clicking any part of an in-world reward chest opens that chest. */
public class ActionCatacombsRewardChest implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onInteract(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (event.getTarget() instanceof RewardChestStand stand) stand.click(player);
    }
}
