package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.skyblockgeneric.event.actions.player.ActionPlayerChangeHypixelMenuDisplay;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Sets the SkyBlock main menu display whenever the Hypixel inventory GUI opens
public class ActionDisplayMenu implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(HypixelInventoryGUI.InventoryGUIOpenEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        ActionPlayerChangeHypixelMenuDisplay.setMainMenu(player);
    }
}
