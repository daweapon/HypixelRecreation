package net.swofty.type.island.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Jerry the Assistant's Patch Notes menu; entries come from configuration/skyblock/patch_notes.yml.
public class GUIPatchNotes extends HypixelInventoryGUI {
    private static final int[] PATCH_SLOTS = {10, 11, 12, 13, 14, 15, 16};

    public GUIPatchNotes() {
        super("Patch Notes", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);

        set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIJerry().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To Jerry The Assistant""");
            }
        });

        List<PatchNote> notes = PatchNote.load();
        for (int i = 0; i < notes.size() && i < PATCH_SLOTS.length; i++) {
            PatchNote note = notes.get(i);
            set(new GUIClickableItem(PATCH_SLOTS[i]) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    p.closeInventory();
                    p.sendMessage(Text.of("<a>SkyBlock v{} <8>- <f>{}", note.version(), note.title()));
                    if (!note.url().isBlank()) {
                        p.sendMessage(Text.of("<click:url:'" + note.url() + "'><e><l>CLICK HERE <e>to view the patch notes!"));
                    }
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(note.material(), """
                            <a>SkyBlock v{}
                            <7>{}

                            <e>Click to view!""", note.version(), note.date());
                }
            });
        }
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
