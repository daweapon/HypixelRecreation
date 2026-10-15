package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.tool.ToolInteractions;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionToolInteractions implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onUse(PlayerUseItemEvent event) {
        SkyBlockItem item = new SkyBlockItem(event.getPlayer().getItemInMainHand());
        if (item.isAir() || !ToolInteractions.isInteractive(item.getAttributeHandler().getTypeAsString())) return;
        ToolInteractions.use((SkyBlockPlayer) event.getPlayer(), item, null);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBlockUse(PlayerBlockInteractEvent event) {
        SkyBlockItem item = new SkyBlockItem(event.getPlayer().getItemInMainHand());
        if (item.isAir() || !ToolInteractions.isInteractive(item.getAttributeHandler().getTypeAsString())) return;
        ToolInteractions.use((SkyBlockPlayer) event.getPlayer(), item, event.getBlockPosition());
    }
}
