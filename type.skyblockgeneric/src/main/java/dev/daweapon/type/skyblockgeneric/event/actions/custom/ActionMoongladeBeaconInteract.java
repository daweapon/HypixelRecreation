package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.moonglade.GUIMoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;

public class ActionMoongladeBeaconInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !event.getBlock().compare(Block.BEACON) || !MoongladeBeacon.onBeaconServer()) return;
        event.setCancelled(true);
        MoongladeBeacon.start();
        ((SkyBlockPlayer) event.getPlayer()).openView(new GUIMoongladeBeacon());
    }
}
