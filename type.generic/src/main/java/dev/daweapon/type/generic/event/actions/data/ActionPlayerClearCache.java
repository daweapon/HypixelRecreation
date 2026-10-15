package dev.daweapon.type.generic.event.actions.data;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import dev.daweapon.type.generic.entity.hologram.PlayerHolograms;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.gui.HypixelAnvilGUI;
import dev.daweapon.type.generic.gui.HypixelSignGUI;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.generic.user.categories.CustomGroups;

public class ActionPlayerClearCache implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void run(PlayerDisconnectEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        // Remove from caches
        CustomGroups.staffMembers.remove(player);
        HypixelNPC.getPerPlayerNPCs().remove(player.getUuid());
        if (HypixelSignGUI.signGUIs.containsKey(player)) {
            HypixelSignGUI.signGUIs.get(player).future().complete(null);
            HypixelSignGUI.signGUIs.remove(player);
        }
        if (HypixelAnvilGUI.anvilGUIs.containsKey(player)) {
            HypixelAnvilGUI.anvilGUIs.get(player).getValue().complete(null);
            HypixelAnvilGUI.anvilGUIs.remove(player);
        }
        if (HypixelInventoryGUI.GUI_MAP.containsKey(player.getUuid())) {
            HypixelInventoryGUI.GUI_MAP.get(player.getUuid()).suddenlyQuit(
                    HypixelInventoryGUI.GUI_MAP.get(player.getUuid()).getInventory(),
                    player);
            HypixelInventoryGUI.GUI_MAP.remove(player.getUuid());
        }
        HypixelNPC.removeDialogueCache(player);
        PlayerHolograms.remove(player);

        // Remove external player holograms associated with the player
        PlayerHolograms.externalPlayerHolograms.entrySet().removeIf(entry -> {
            PlayerHolograms.ExternalPlayerHologram hologram = entry.getKey();
            if (hologram.getPlayer().equals(player)) {
                PlayerHolograms.removeExternalPlayerHologram(hologram);
                return true;
            }
            return false;
        });
    }
}
