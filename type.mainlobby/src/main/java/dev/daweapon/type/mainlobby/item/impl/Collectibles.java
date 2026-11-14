package dev.daweapon.type.mainlobby.item.impl;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.lobby.item.LobbyItem;

// Main Lobby hotbar item that opens the (unimplemented) Collectibles menu.
public class Collectibles extends LobbyItem {

    public Collectibles() {
        super("collectibles");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.CHEST, "<a>Collectibles <7>(Right Click)").build();
    }

    @Override
    public void onItemDrop(ItemDropEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        ((HypixelPlayer) event.getPlayer()).notImplemented();
        ((CancellableEvent) event).setCancelled(true);
    }

}
