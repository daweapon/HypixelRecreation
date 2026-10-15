package dev.daweapon.type.skyblockgeneric.event.actions.item;

import net.minestom.server.event.item.ItemDropEvent;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.drop.ItemDrops;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.garden.tool.FarmingToolkit;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Handles item drops: blocks menu items, shows a one-time privacy notice, and throws the dropped entity
public class ActionItemDrop implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(ItemDropEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (new SkyBlockItem(event.getItemStack()).getAttributeHandler().getTypeAsString().toLowerCase().contains("menu")) {
            event.setCancelled(true);
            return;
        }

        if (FarmingToolkit.slotOf(new SkyBlockItem(event.getItemStack()).getAttributeHandler().getTypeAsString()) != null) {
            event.setCancelled(true);
            player.sendMessage("<c>You can't drop Farming Tools! Store them in your <e>/farmingtoolkit<c> instead.");
            return;
        }

        if (player.getOpenInventory() != null) {
            event.setCancelled(true);
            return;
        }

        if (player.isInLaunchpad()) {
            event.setCancelled(true);
            return;
        }

        boolean hideMessage = player.getToggles().get(DatapointToggles.Toggles.ToggleType.DISABLE_DROP_MESSAGES);

        if (!hideMessage) {
            player.sendMessage("<hover:'<e>Click here to disable the alert!'><click:run:'/toggledropalert'><e>⚠ <a>Your drops can't be seen by other players in <b>SkyBlock<a>!</click></hover>");
            player.sendMessage("<hover:'<e>Click here to disable the alert!'><click:run:'/toggledropalert'><a>Only you can pickup your dropped items!</click></hover>");
            player.sendMessage("<hover:'<e>Click here to disable the alert!'><click:run:'/toggledropalert'><e>Click here to disable this alert forever!</click></hover>");
        }

        ItemDrops.throwFromPlayer(player, new DroppedItemEntityImpl(
                new SkyBlockItem(event.getItemStack()), player));
    }
}
