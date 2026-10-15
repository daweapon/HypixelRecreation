package dev.daweapon.type.catacombs.events;

import dev.daweapon.type.catacombs.run.CatacombsItems;
import dev.daweapon.type.catacombs.run.CatacombsRun;
import dev.daweapon.type.catacombs.run.CatacombsRuns;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;

/** The ready-up dye: using it flips the player's ready state in their run. */
public class ActionCatacombsInput implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onUse(PlayerUseItemEvent event) {
        if (!CatacombsItems.isReadyItem(event.getItemStack())) return;
        event.setCancelled(true);
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        CatacombsRun run = CatacombsRuns.of(player.getUuid());
        if (run != null) run.toggleReady(player);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onBeginUse(PlayerBeginItemUseEvent event) {
        if (CatacombsItems.isReadyItem(event.getItemStack())) event.setCancelled(true);
    }
}
