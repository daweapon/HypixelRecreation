package dev.daweapon.type.skywarsgame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import dev.daweapon.type.skywarsgame.TypeSkywarsGameLoader;
import dev.daweapon.type.skywarsgame.game.SkywarsGame;
import dev.daweapon.type.skywarsgame.item.SimpleInteractableItem;
import dev.daweapon.type.skywarsgame.user.SkywarsPlayer;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;

public class LeaveGameBed extends SimpleInteractableItem {

    public LeaveGameBed() {
        super("leave_game");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.RED_BED, 1, """
                <c><l>Return to Lobby </l><7>(Right Click)
                <7>Right-click to leave to the lobby!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(event.getPlayer());
        if (game != null) {
            game.leave((SkywarsPlayer) event.getPlayer());
        }
    }
}
