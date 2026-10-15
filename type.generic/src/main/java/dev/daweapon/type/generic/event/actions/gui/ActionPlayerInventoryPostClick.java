package dev.daweapon.type.generic.event.actions.gui;

import net.minestom.server.event.inventory.InventoryClickEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.item.GUIClickableItem;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class ActionPlayerInventoryPostClick implements HypixelEventClass {

	@PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
	public void run(InventoryClickEvent event) {
		final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

		if (HypixelInventoryGUI.GUI_MAP.containsKey(player.getUuid())) {
			HypixelInventoryGUI gui = HypixelInventoryGUI.GUI_MAP.get(player.getUuid());

			if (gui == null) return;

			int slot = event.getSlot();
			GUIItem item = gui.get(slot);

			if (item == null) return;

			if (item instanceof GUIClickableItem clickable) {
				clickable.runPost(event, player);
			}
		}
	}
}
