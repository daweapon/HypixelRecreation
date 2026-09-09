package net.swofty.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;

// GUI showing museum donation reward tiers, with hotkeying and bottom-click disabled
public class GUIMuseumRewards extends HypixelInventoryGUI {
    public GUIMuseumRewards() {
        super(Text.key("gui_museum.rewards.title"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
