package net.swofty.type.skyblockgeneric.gui.inventories.museum;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;

// GUI shown while a museum item is being appraised, with hotkeying and bottom-click disabled
public class GUIMuseumAppraisal extends HypixelInventoryGUI {
    public GUIMuseumAppraisal() {
        super(Text.key("gui_museum.appraisal.title"), InventoryType.CHEST_5_ROW);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
