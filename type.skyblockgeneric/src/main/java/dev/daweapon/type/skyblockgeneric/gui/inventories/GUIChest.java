package dev.daweapon.type.skyblockgeneric.gui.inventories;


import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.HypixelInventoryGUI;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.inventory.item.GUIItem;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chest.Chest;
import dev.daweapon.type.skyblockgeneric.chest.ChestAnimationType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.IntStream;

// GUI backing a placeable storage chest, persisting its contents and playing open/close effects
public class GUIChest extends HypixelInventoryGUI {

    private final Chest chest;

    public GUIChest(Chest chest) {
        super(Text.literal(chest.getName()), chest.getSize());
        this.chest = chest;
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        IntStream.range(0, chest.getItems().size()).forEach(index -> {
            set(new GUIItem(index) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.copy(chest.getItem(index));
                }

                @Override
                public boolean canPickup() {
                    return true;
                }
            });
        });

    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        e.getPlayer().playSound(Sound.sound(SoundEvent.BLOCK_CHEST_CLOSE, Sound.Source.RECORD, 1f, 1f));
        ChestAnimationType.CLOSE.play(chest.getInstance(), chest.getPosition());

        AbstractInventory inventory = e.getInventory();
        IntStream.range(0, inventory.getItemStacks().length).forEach(i -> chest.setItem(i, inventory.getItemStack(i)));
    }

    @Override
    public boolean allowHotkeying() {
        return true;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
