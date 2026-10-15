package dev.daweapon.type.bedwarsgame.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.bedwarsgame.gui.GUITrackerAndCommunication;
import dev.daweapon.type.bedwarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;

// The default compass item that opens the tracker/communication menu on interact.
public class CompassItem extends SimpleInteractableItem {

    public CompassItem() {
        super("compass");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.named(Material.COMPASS, Text.key("bedwars.item.compass")).build();
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        player.openView(new GUITrackerAndCommunication());
    }
}
