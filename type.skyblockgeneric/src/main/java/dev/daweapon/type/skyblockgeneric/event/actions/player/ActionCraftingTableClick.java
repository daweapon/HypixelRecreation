package dev.daweapon.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.GUICrafting;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Opens the crafting GUI when a player interacts with a crafting table
public class ActionCraftingTableClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (Material.fromKey(event.getBlock().key()) != Material.CRAFTING_TABLE) {
            return;
        }

        event.setBlockingItemUse(true);
        player.openView(new GUICrafting());
    }
}

