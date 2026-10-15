package dev.daweapon.type.bedwarsgame.item.impl;

import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.bedwarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.user.HypixelPlayer;

// Spectator settings hotbar item (feature stub, not yet implemented).
public class SpectatorSettingsItem extends SimpleInteractableItem {


    public SpectatorSettingsItem() {
        super("spectator_settings");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.COMPASS, """
                <b><l>Spectator Settings </l><7>(Right Click)
                <7>Right-click to change your spectator settings!""").build();
    }

    @Override
    public void onItemInteract(PlayerInstanceEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        player.sendMessage("<click:url:'https://github.com/daweapon/HypixelRecreation'><c>This Feature is not there yet. <a>Open a Pull request HERE to get it added quickly!");
    }
}
