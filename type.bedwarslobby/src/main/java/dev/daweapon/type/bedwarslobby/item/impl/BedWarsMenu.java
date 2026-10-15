package dev.daweapon.type.bedwarslobby.item.impl;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarslobby.gui.cosmetics.GUIBedWarsMenuShop;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.lobby.item.LobbyItem;

// Lobby hotbar item that opens the Bedwars menu/shop GUI on interact.
public class BedWarsMenu extends LobbyItem {

    public BedWarsMenu() {
        super("bedwars_menu");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.named(Material.EMERALD, "<a>Bed Wars Menu & Shop <7>(Right Click)").build();
    }

    @Override
    public void onItemDrop(ItemDropEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        if (event instanceof CancellableEvent cancellableEvent) {
            cancellableEvent.setCancelled(true);
        }
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        player.openView(new GUIBedWarsMenuShop());
    }

}
